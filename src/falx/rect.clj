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
     (<= x x2 (+ x w -1))
     (<= y y2 (+ y h -1)))))

(defn get-centered-rect-point
  ([rect size]
    (let [[w2 h2] size]
      (get-centered-rect-point rect w2 h2)))
  ([rect w2 h2]
    (let [[x y w h] rect]
      (get-centered-rect-point x y w h w2 h2)))
  ([x y w h w2 h2]
   [(int (+ x (/ (- w w2) 2)))
    (int (+ y (/ (- h h2) 2)))]))

(defn get-centered-rect
  ([rect size]
   (let [[w2 h2] size]
     (get-centered-rect rect w2 h2)))
  ([rect w2 h2]
   (let [[x y w h] rect]
     (get-centered-rect x y w h w2 h2)))
  ([x y w h w2 h2]
   [(int (+ x (/ (- w w2) 2)))
    (int (+ y (/ (- h h2) 2)))
    w2
    h2]))