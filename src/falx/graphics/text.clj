(ns falx.graphics.text
  (:require [falx.application :as app]
            [falx.graphics.color :as color])
  (:import (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch BitmapFont$TextBounds)
           (com.badlogic.gdx.graphics Color)))

(def font (delay (BitmapFont. true)))

(defn draw!
  [text x y]
  (let [^SpriteBatch batch @app/sprite-batch]
    (.draw @font batch (str text) (float x) (float y))))

(defn measure
  [text]
  (let [^BitmapFont$TextBounds bounds (.getBounds @font (str text))]
    [(.width bounds)
     (.height bounds)]))

(defn get-centered-point
  ([text rect]
    (let [[x y w h] rect]
      (get-centered-point text x y w h)))
  ([text x y w h]
    (let [[w2 h2] (measure text)]
      [(int (+ x (/ (- w w2) 2)))
       (int (+ y (/ (- h h2) 2)))])))

(defmacro with-color
  [color & forms]
  `(let [f# @font
         o# (.getColor f#)
         ^Color c# (color/find-color ~color)]
     (.setColor f# c#)
     ~@forms
     (.setColor f# o#)))