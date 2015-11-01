(ns falx.graphics.widgets
  (:require [falx.graphics.shape :as shape]
            [falx.graphics.text :as text]
            [falx.graphics.image :as image]
            [falx.frame :as frame]
            [falx.mouse :as mouse]
            [falx.point :as point]))

(defmacro with-color
  [color & body]
  `(let [c# ~color]
     (image/with-color
       c#
       (text/with-color
         c#
         ~@body))))

(defn mouse-captured?
  ([frame rect]
    (mouse-captured? frame rect))
  ([frame x y w h]
   (mouse/in-rectangle? (frame/get-screen-mouse frame) x y w h)))

(defn draw-text-button!*
  [text x y w h]
  (shape/draw-box! x y w h)
  (text/draw-centered! text x y w h))

(defn draw-text-button!
  ([frame text rect]
    (let [[x y w h] rect]
      (draw-text-button! frame text x y w h)))
  ([frame text x y w h]
   (if (mouse-captured? frame x y w h)
     (with-color
       :green
       (draw-text-button!* text x y w h))
     (draw-text-button!* text x y w h))))

(defn draw-sprite-button!
  ([frame sprite x y w h]
    (if (mouse-captured? frame x y w h)
      (with-color
        :white
        (image/draw! sprite x y w h)))))

(defn draw-scroll-down!
  ([point]
   (draw-scroll-down! (point/get-x point) (point/get-y point)))
  ([x y]
   (image/draw! :scroll-down x y)))

(defn draw-scroll-up!
  ([point]
   (draw-scroll-up! (point/get-x point) (point/get-y point)))
  ([x y]
   (image/draw! :scroll-up x y)))



