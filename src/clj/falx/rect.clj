(ns falx.rect
  (:require [falx.point :as pt]))

(def zero [0 0 0 0])

(defn x [r] (nth r 0))
(defn y [r] (nth r 1))
(defn w [r] (nth r 2))
(defn h [r] (nth r 3))

(defn point [r] [(x r) (y r)])
(defn size [r] [(w r) (h r)])
(defn top-left [r] (point r))
(defn top-right [r] [(+ (x r) (w r)) (y r)])
(defn bottom-left [r] [(x r) (+ (y r) (h r))])
(defn bottom-right [r] [(+ (x r) (w r)) (+ (y r) (h r))])

(defn fmap [f r] [(f (x r)) (f (y r)) (f (w r)) (f (h r))])

(defn contains-point?
  ([r p]
   (and (<= (x r) (pt/x p) (+ (x r) (w r) -1))
        (<= (y r) (pt/y p) (+ (y r) (h r) -1)))))

(defn contains-loc?
  ([x y w h loc]
   (let [[x2 y2] loc]
     (contains-loc? x y w h x2 y2)))
  ([x y w h x2 y2]
   (and (<= x x2 (+ x w -1))
        (<= y y2 (+ y h -1)))))
