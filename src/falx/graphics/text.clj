(ns falx.graphics.text
  (:require [falx.application :as app]
            [falx.graphics.color :as color])
  (:import (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch)
           (com.badlogic.gdx.graphics Color)))

(def font (delay (BitmapFont. true)))

(defn draw!
  [text x y]
  (let [^SpriteBatch batch @app/sprite-batch]
    (.draw @font batch (str text) (float x) (float y))))

(defmacro with-color
  [color & forms]
  `(let [f# @font
         o# (.getColor f#)
         ^Color c# (color/find-color ~color)]
     (.setColor f# c#)
     ~@forms
     (.setColor f# o#)))