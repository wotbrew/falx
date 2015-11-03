(ns gdx
  (:require [gdx.app :as app]
            [gdx.display :as display]
            [gdx.batch :as batch]
            [gdx.color :as color]
            [gdx.camera :as camera]
            [gdx.gl :as gl]
            [gdx.font :as font]
            [gdx.texture-region :as region]
            [gdx.texture :as texture]
            [gdx.mouse :as mouse]
            [gdx.keyboard :as keyboard]
            [clojure.java.io :as io])
  (:import (com.badlogic.gdx.graphics.g2d SpriteBatch TextureRegion)
           (java.util Map)))

(def default-display display/default)

(def default-app app/default)

(defn get-display
  []
  (app/on-render-thread
    (display/get-current)))

(defn sync-display!
  [display]
  (app/on-render-thread
    (display/sync! display)))

(defn start-app!
  [app]
  (app/start! app))

(def ^:dynamic *sprite-batch* nil)

(defonce default-sprite-batch (delay (app/on-render-thread (SpriteBatch.))))

(defmacro using-batch
  [batch & body]
  `(binding [*sprite-batch* ~batch]
    (batch/using *sprite-batch* ~@body)))

(defmacro using-default-batch
  [& body]
  `(using-batch @default-sprite-batch ~@body))

(defmacro using-camera
  [camera & body]
  `(camera/using-camera
     *sprite-batch*
     ~camera
     ~@body))

(def default-camera
  (camera/ortho-camera
    :flip-y? true))

(def keyboard-state (atom (keyboard/get-keyboard)))

(def mouse-state (atom (mouse/get-mouse)))

(defmacro defrender
  [& body]
  `(app/defrender
     (swap! keyboard-state keyboard/get-next-keyboard)
     (swap! mouse-state mouse/get-next-mouse)
     (using-default-batch
       (using-camera
         ~default-camera
         (gl/clear!)
         ~@body))))

(defn font
  ([]
    (font/font :flip-y? true))
  ([file]
    (font/font :file file
               :flip-y? true)))

(def default-font
  (font))

(defn sprite
  [file rect]
  (let [texture (texture/texture file)]
    (region/texture-region
      texture
      rect
      :flip-y? true)))

(defmacro using-color
  [color & body]
  `(let [c# ~color]
     (if c#
       (batch/using-color-float-bits
         *sprite-batch*
         (:float-bits c# (color/get-float-bits c#))
         ~@body)
       (do ~@body))))

(defmacro using-texture-region-context
  [context & body]
  `(let [context# ~context]
     (using-color (:color context#) ~@body)))

(defprotocol IDraw
  (-draw! [this x y context]))

(defn draw!
  ([thing point]
   (draw! thing point {}))
  ([thing point context]
   (let [x (nth point 0)
         y (nth point 1)]
     (-draw! thing x y context))))

(defmulti draw-map! (fn [m x y context] (:type m)))

(defmethod draw-map! :default
  [m x y context]
  (-draw! (pr-str m) x y context))

(extend-protocol IDraw
  Object
  (-draw! [this x y context]
    (batch/draw-string!
      *sprite-batch*
      (font/find (:font context default-font))
      this
      x y))
  nil
  (-draw! [this x y context]
    (-draw! "?" x y context))
  Map
  (-draw! [this x y context]
    (draw-map! this x y context))
  TextureRegion
  (-draw! [this x y context]
    (using-texture-region-context
      context
      (batch/draw-texture-region! *sprite-batch* this x y))))

(defmethod draw-map! :resource/texture-region
  [region x y context]
  (-draw! (region/find region) x y context))

(defrender
  (try
    (draw! "foobar" [500 400])
    (draw! (app/get-fps) [0 0])
    (draw! @mouse-state [0 32])
    (draw! @keyboard-state [0 64])
    (catch Throwable e
      (println e)
      (Thread/sleep 10000))))
