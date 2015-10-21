(ns falx.rect)

(def default [0 0 0 0])

(def get-x #(nth % 0))

(def get-y #(nth % 1))

(def get-width #(nth % 2))

(def get-height #(nth % 3))

(defn get-size
  [rect]
  [(get-width rect)
   (get-height rect)])

(defn contains-point?
  ([rect point]
   (let [[x y w h] rect]
     (contains-point? x y w h point)))
  ([x y w h point]
   (let [[x2 y2] point]
     (contains-point? x y w h x2 y2)))
  ([x y w h x2 y2]
   (and
     (<= x x2 (+ x w))
     (<= y y2 (+ y h)))))