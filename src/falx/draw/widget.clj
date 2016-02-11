(ns falx.draw.widget
  (:require [falx.draw :as draw]
            [falx.draw.sprite :as sprite]
            [clj-gdx :as gdx]
            [gdx.color :as color]
            [falx.size :as size]))

(defmethod draw/drawm! :ui.type/text
  [{:keys [text rect]} x2 y2]
  (let [[x y w] rect]
    (gdx/draw-string! text (+ x x2) (+ y y2) w)))

(defn draw-centered-string!
  [s x y w h]
  (let [bounds (gdx/get-string-wrapped-bounds s w)
        [x y w] (size/centered-rect bounds x y w h)]
    (gdx/draw-string! s x y w)))

(defmethod draw/drawm! :ui.type/centered-text
  [{:keys [text rect]} x2 y2]
  (let [[x y w h] rect]
    (draw-centered-string! text (+ x x2) (+ y y2) w h)))

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

(defmethod draw/drawm! :ui.type/wrap
  [{:keys [element]} x2 y2]
  (draw/draw! element x2 y2))

(defmethod draw/drawm! :ui.type/backing
  [{:keys [rect]} x2 y2]
  (let [[x y w h] rect]
    (gdx/draw-sprite! sprite/pixel (+ x x2) (+ y y2) w h {:color color/black})))

(defmethod draw/drawm! :ui.type/block
  [{:keys [rect]} x2 y2]
  (let [[x y w h] rect
        x (int x)
        y (int y)
        w (int w)
        h (int h)
        xl (int (+ x x2 w))
        yl (int (+ y y2 h))]
    (loop [x (+ x x2)]
      (when (< x xl)
        (loop [y (+ y y2)]
          (when (< y yl)
            (gdx/draw-sprite! sprite/block x y 32 32)
            (recur (+ y 32))))
        (recur (+ x 32))))))

(defmethod draw/drawm! :ui.type/arrow-right
  [{:keys [rect]} x2 y2]
  (let [[x y w h] rect]
    (draw-centered-string! ">" (+ x x2) (+ y y2) w h)))

(defmethod draw/drawm! :ui.type/arrow-left
  [{:keys [rect]} x2 y2]
  (let [[x y w h] rect]
    (draw-centered-string! "<" (+ x x2) (+ y y2) w h)))