(ns falx.gdx.font
  (:require [falx.gdx.impl.io :as io])
  (:import (com.badlogic.gdx.graphics.g2d BitmapFont BitmapFont$TextBounds)
           (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics Color)))

(defn bitmap-font
  ([]
    (BitmapFont. true))
  ([file]
    (BitmapFont. ^FileHandle (io/file-handle file) true)))

(defn str-bounds*
  [font s width]
  (let [^BitmapFont$TextBounds bounds (.getWrappedBounds font (str s) (float width))]
    [(.-width bounds) (.-height bounds)]))

(defn str-bounds
  ([font s]
   (let [^BitmapFont$TextBounds bounds (.getMultiLineBounds font (str s))]
     [(.-width bounds) (.-height bounds)]))
  ([font s size]
   (let [[w h] size]
     (str-bounds* font s w))))

(defn set-color!
  [font color]
  (let [[r g b a] color]
    (.setColor ^BitmapFont font r g b a)))

(defn color
  [font]
  (let [^Color color (.getColor ^BitmapFont font)]
    [(.-r color)
     (.-g color)
     (.-b color)
     (.-a color)]))

(defmacro with-color
  [font color & body]
  `(let [f# ~font
         old-color# (falx.gdx.font/color f#)]
     (set-color! f# ~color)
     (let [r# (do ~@body)]
       (set-color! f# old-color#)
       r#)))
