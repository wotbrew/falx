(ns falx.rect
  (require [falx.point :as point]))

(defn get-points
  ([rect]
   (let [[x y w h] rect]
     (get-points x y w h)))
  ([x y w h]
    (for [x (range x (+ x w))
          y (range y (+ y h))]
      [x y])))

(defn get-edge-points
  ([rect]
   (let [[x y w h] rect]
     (get-edge-points x y w h)))
  ([x y w h]
   (concat
     (take w (point/line-right x y))
     (take w (point/line-right x (+ y h -1)))
     (take h (point/line-down x y))
     (take h (point/line-down (+ x w -1) y)))))