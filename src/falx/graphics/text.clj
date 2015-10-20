(ns falx.graphics.text
  (:require [falx.application :as app])
  (:import (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch)))

(def font (delay (BitmapFont. true)))

(defn draw!
  [text x y]
  (let [^SpriteBatch batch @app/sprite-batch]
    (.draw @font batch (str text) (float x) (float y))))