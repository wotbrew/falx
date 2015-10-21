(ns falx.application
  (:require [clojure.tools.logging :refer [error]])
  (:import (com.badlogic.gdx ApplicationListener Gdx)
           (com.badlogic.gdx.backends.lwjgl LwjglApplication)
           (com.badlogic.gdx.graphics.g2d SpriteBatch)))

(def ^:dynamic *on-render-thread* false)

(def sprite-batch (delay (SpriteBatch.)))

(defn render-call
  [f]
  (binding [*on-render-thread* true]
    (let [^SpriteBatch batch @sprite-batch]
      (.begin batch)
      (try
        (f)
        (catch Throwable e
          (error e "An error occurred running frame")
          (Thread/sleep 5000)))
      (.end batch))))

(defn on-render-thread-call
  [f]
  (if *on-render-thread*
    (delay (f))
    (let [p (promise)
          f' (fn [] (deliver p (try (f) (catch Throwable e [::error e]))))]
      (.postRunnable Gdx/app f')
      (let [r @p]
        (if (and (vector? r) (= (first r) ::error))
          (throw (second r))
          r)))))

(defmacro on-render-thread
  [& body]
  `(if *on-render-thread*
     (do ~@body)
     (on-render-thread-call (fn [] ~@body))))

(defn get-fps
  []
  (.getFramesPerSecond Gdx/graphics))

(defn get-frame-id
  []
  (.getFrameId Gdx/graphics))

(defn get-delta-time
  []
  (.getDeltaTime Gdx/graphics))

(defn listener
  [render-fn]
  (proxy
    [ApplicationListener]
    []
    (render []
      (render-call render-fn))
    (create [])
    (resize [w h])
    (pause [])
    (resume [])
    (dispose [])))

(defn application
  [render-fn]
  (LwjglApplication. (listener render-fn)))
