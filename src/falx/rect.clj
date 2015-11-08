(ns falx.rect
  (:refer-clojure :exclude [extend]))

(def default [0 0 0 0])

(defn contains-point?
  ([rect point]
   (let [[x2 y2] point]
     (contains-point? rect x2 y2)))
  ([rect x2 y2]
   (let [[x y w h] rect]
     (contains-point? x y w h x2 y2)))
  ([x y w h point]
   (let [[x2 y2] point]
     (contains-point? x y w h x2 y2)))
  ([x y w h x2 y2]
   (and
     (< x x2 (+ x w 1))
     (< y y2 (+ y h 1)))))

(defn center-rect
  ([rect size]
   (let [[x y w h] rect]
     (center-rect x y w h size)))
  ([x y w h size]
   (let [[w2 h2] size]
     (center-rect x y w h w2 h2)))
  ([x y w h w2 h2]
   (let [hw (int (/ (- w w2) 2))
         hh (int (/ (- h h2) 2))]
     [(+ x hw)
      (+ y hh)
      w2
      h2])))

(defn extend
  ([rect n]
    (let [[x y w h] rect]
      (extend x y w h n)))
  ([x y w h n]
    [(- x n)
     (- y n)
     (+ w n n)
     (+ h n n)]))

(defn fit-horizontal
  ([rect n]
   (let [[x y w h] rect]
     (fit-horizontal x y w h n)))
  ([x y w h n]
   (let [i (int (/ w n))]
     (map (fn [n]
            [(+ x (* n i)) y i h])
          (range n)))))

(defn fit-vertical
  ([rect n]
    (let [[x y w h] rect]
      (fit-vertical x y w h n)))
  ([x y w h n]
    (let [i (int (/ h n))]
      (map (fn [n]
             [x (+ y (* n i)) w i])
           (range n)))))

(defn cycle-vertical
  ([rect n]
   (let [[x y w h] rect]
     (cycle-vertical x y w h n)))
  ([x y w h n]
   (lazy-seq
     (cons
       [x y w h]
       (cycle-vertical x (+ y n) w h n)))))