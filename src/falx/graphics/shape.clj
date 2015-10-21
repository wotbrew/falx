(ns falx.graphics.shape
  (:require [falx.graphics.image :as image]))

(def ^:dynamic *default-horizontal-thickness* 1)
(def ^:dynamic *default-vertical-thickness* 1)

(defn draw-pixel!
  ([point-or-rect]
    (if (= (count point-or-rect) 4)
      (let [[x y w h] point-or-rect]
        (draw-pixel! x y w h))
      (let [[x y] point-or-rect]
        (draw-pixel! x y))))
  ([x y]
    (draw-pixel! x y 1 1))
  ([x y w h]
    (image/draw! :pixel x y w h)))

(defn draw-vertical-line!
  ([point length]
   (let [[x y] point]
     (draw-vertical-line! x y length)))
  ([x y length]
   (draw-vertical-line! x y length *default-horizontal-thickness*))
  ([x y length thickness]
   (draw-pixel! x y thickness length)))

(defn draw-horizontal-line!
  ([point length]
   (let [[x y] point]
     (draw-horizontal-line! x y length)))
  ([x y length]
   (draw-horizontal-line! x y length *default-vertical-thickness*))
  ([x y length thickness]
   (draw-pixel! x y length thickness)))

(def ^:dynamic *default-box-thickness* 1)

(defn draw-box!
  ([rect]
    (draw-box! rect *default-box-thickness*))
  ([rect thickness]
   (let [[x y w h] rect]
     (draw-box! x y w h thickness)))
  ([x y w h]
    (draw-box! x y w h *default-box-thickness*))
  ([x y w h thickness]
    (draw-horizontal-line! x y w thickness)
    (draw-horizontal-line! x (+ y h) w thickness)
    (draw-vertical-line! x y h thickness)
    (draw-vertical-line! (+ x w) y h thickness)))