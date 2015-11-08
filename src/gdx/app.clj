(ns gdx.app
  (:require [gdx.display :as display]
            [gdx.dispatch :as dispatch])
  (:import (com.badlogic.gdx ApplicationListener Gdx Graphics)))

(def on-render-fn (atom nil))
(def on-create-fn (atom nil))
(def on-dispose-fn (atom nil))
(def on-resume-fn (atom nil))
(def on-pause-fn (atom nil))
(def on-resize-fn (atom nil))

(defmacro defrender
  [& body]
  `(reset! on-render-fn (fn [] ~@body)))

(defmacro defcreate
  [& body]
  `(reset! on-create-fn (fn [] ~@body)))

(defmacro defdispose
  [& body]
  `(reset! on-dispose-fn (fn [] ~@body)))

(defmacro defpause
  [& body]
  `(reset! on-pause-fn (fn [] ~@body)))

(defmacro defresume
  [& body]
  `(reset! on-resume-fn (fn [] ~@body)))

(defmacro defresize
  [size-binding & body]
  `(reset! on-resize-fn (fn ~size-binding ~@body)))

(def listener-proxy
  (proxy
    [ApplicationListener]
    []
    (render []
      (when-let [f @on-render-fn]
        (binding [dispatch/*on-render-thread* true]
          (f))))
    (create []
      (when-let [f @on-create-fn]
        (f)))
    (dispose []
      (when-let [f @on-dispose-fn]
        (f)))
    (resume []
      (when-let [f @on-resume-fn]
        (f)))
    (pause []
      (when-let [f @on-pause-fn]
        (f)))
    (resize [width height]
      (when-let [f @on-resize-fn]
        (f [width height])))))

(def default
  {:type :application/lwjgl
   :display display/default
   :max-foreground-fps 60
   :max-background-fps 30})

(defmulti start!* :type)

(defmethod start!* :default
  [app]
  (throw (ex-info "No backend loaded for app type" app)))

(defn start!
  [app]
  (let [type (:type app)
        backend-name (name type)]
    (try
      (require [(symbol (str "gdx.backends." backend-name))])
      (catch Throwable e))
    (start!* app)))

(defn ensure-app-exists!
  []
  (or Gdx/app (start! default)))

(defn on-render-thread-call
  [f]
  (ensure-app-exists!)
  (dispatch/on-render-thread-call f))

(defmacro on-render-thread
  [& body]
  `(if dispatch/*on-render-thread*
     (do ~@body)
     @(on-render-thread-call (fn [] ~@body))))

(defn ^Graphics get-graphics
  []
  (when Gdx/app
    (.getGraphics Gdx/app)))

(defn get-fps
  []
  (when-some [gfx (get-graphics)]
    (.getFramesPerSecond gfx)))

(defn get-delta-time
  []
  (when-some [gfx (get-graphics)]
    (.getDeltaTime gfx)))

(defn get-frame-id
  []
  (when-some [gfx (get-graphics)]
    (.getFrameId gfx)))