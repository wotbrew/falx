(ns falx.gdx
  (:require [falx.gdx.impl.dispatch :as dispatch]
            [falx.gdx.impl.app :as app]
            [falx.gdx.impl.signal :as signal]
            [falx.gdx.batch :as batch]
            [falx.gdx.camera :as cam]
            [falx.gdx.display :as display]
            [falx.gdx.font :as font]
            [falx.gdx.texture :as texture])
  (:import (com.badlogic.gdx Gdx Graphics)
           (com.badlogic.gdx.graphics.g2d SpriteBatch)))

(defmacro signal
  "A signal represents a value that is sampled every frame.
   A signal is both deref'able and invokable."
  [& body]
  `(signal/signal ~@body))

(defn- ^Graphics graphics
  []
  (when Gdx/app
    (.getGraphics Gdx/app)))

(def frame-id
  (signal
    (when-some [gfx (graphics)]
      (.getFrameId gfx))))

(def fps
  (signal
    (when-some [gfx (graphics)]
      (.getFramesPerSecond gfx))))

(def delta-time
  (signal
    (when-some [gfx (graphics)]
      (.getDeltaTime gfx))))

(defn started?
  []
  (some? Gdx/app))

(def batch
  (delay
    (dispatch/dispatch
      (SpriteBatch.))))

(defmacro with-font-color
  ([font color & body]
   `(font/with-color
      ~font ~color ~@body)))

(defn draw-str!
  ([s font rect]
   (batch/draw-str! @batch font (str s) rect))
  ([s font x y w]
   (batch/draw-str! @batch font (str s) x y w)))

(defn draw-str-at!
  ([s font pt]
   (batch/draw-str-at! @batch font (str s) pt))
  ([s font x y]
   (batch/draw-str-at! @batch font (str s) x y)))

(defn draw-region!
  ([region rect]
   (batch/draw-region! @batch region rect))
  ([region x y w h]
   (batch/draw-region! @batch region x y w h)))

(defn draw-region-at!
  ([region pt]
   (batch/draw-region-at! @batch region pt))
  ([region x y]
   (batch/draw-region-at! @batch region x y)))

(defmacro with-region-color
  [color & body]
  `(batch/with-color
     @batch
     ~color
     ~@body))

(defn camera
  [size]
  (cam/camera size))

(def cam
  (delay
    (camera [800 600])))

(defmacro defrender
  [& body]
  `(reset!
     app/on-render-fn
     (fn []
       (try
         (display/clear!)
         (let [cam# @cam
               batch# @batch]
           (batch/with batch#
             (cam/set-size! cam# (display/size))
             (cam/with batch# cam#
               ~@body)))
         (catch Throwable e#
           (println e#)
           (Thread/sleep 5000))))))

(defn str-bounds*
  ([s font width]
   (font/str-bounds* font (str s) width)))

(defn str-bounds
  ([s font]
   (font/str-bounds font (str s) ))
  ([s font size]
   (font/str-bounds font (str s) size)))

(def ^:private texture*
  (memoize texture/texture))

(defn texture
  ([file]
   (texture* file)))

(defn region
  ([file rect]
   (texture/region (texture file) rect))
  ([file x y w h]
   (texture/region (texture file) x y w h)))

(def ^:private bitmap-font*
  (memoize font/bitmap-font))

(defn bitmap-font
  ([]
   (bitmap-font*))
  ([file]
   (bitmap-font* file)))

(defn start-lwjgl!
  ([]
   (start-lwjgl! nil))
  ([opts]
   (require 'falx.gdx.impl.lwjgl)
   ((ns-resolve 'falx.gdx.impl.lwjgl 'app) opts)))