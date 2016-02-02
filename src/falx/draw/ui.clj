(ns falx.draw.ui
  (:require [falx.draw :as draw]
            [falx.draw.sprite :as sprite]
            [clj-gdx :as gdx]))

(defmethod draw/drawm! :ui.type/text
  [{:keys [text rect]} x2 y2]
  (let [[x y w] rect]
    (gdx/draw-string! text (+ x x2) (+ y y2) w)))

(defmethod draw/drawm! :ui.type/box
  [{:keys [rect]} x2 y2]
  (let [[x y w h] rect
        x (+ x x2)
        y (+ y y2)]
    (gdx/draw-sprite! sprite/pixel x y w 1)
    (gdx/draw-sprite! sprite/pixel x y 1 h)
    (gdx/draw-sprite! sprite/pixel x (+ y h) w 1)
    (gdx/draw-sprite! sprite/pixel (+ x w) y 1 h)))

(defmethod draw/drawm! :ui.type/panel
  [{:keys [rect elements]} x2 y2]
  (let [[x y] (or rect [0 0 0 0])
        x (+ x x2)
        y (+ y y2)]
    (run! #(draw/draw! % x y) elements)))