(ns falx.point)

(def zero [0 0])
(def one [1 1])

(def north [0 -1])
(def north-east [1 -1])
(def east [1 0])
(def south-east [1 1])
(def south [0 1])
(def south-west [-1 1])
(def west [-1 0])
(def north-west [-1 -1])

(def up north)
(def right east)
(def left west)
(def down south)

(def directions
  [north
   north-east
   east
   south-east
   south
   south-west
   west
   north-west])

(def cardinals
  [north
   east
   south
   west])

(defn x [p] (if (number? p) p (nth p 0)))
(defn y [p] (if (number? p) p (nth p 1)))

(defn fmap [f p] [(f (x p)) (f (y p))])

(defn dpoint [p] (fmap double p))
(defn ipoint [p] (fmap long p))

(defn add
  ([] zero)
  ([p] p)
  ([p1 p2]
   [(+ (x p1) (x p2))
    (+ (y p1) (y p2))])
  ([p1 p2 p3]
   [(+ (x p1) (x p2) (x p3)) (+ (y p1) (y p2) (y p3))])
  ([p1 p2 p3 & more]
   [(apply + (x p1) (x p2) (x p3) (map x more))
    (apply + (y p1) (y p2) (y p3) (map y more))]))

(defn addx
  ([p] p)
  ([p1 p2]
   [(+ (x p1) (x p2)) (y p1)])
  ([p1 p2 p3]
   [(+ (x p1) (x p2) (x p3)) (y p1)])
  ([p1 p2 p3 & more]
    [(apply + (x p1) (x p2) (x p3) (map x more))
     (y p1)]))

(defn addy
  ([p] p)
  ([p1 p2]
   [(x p1) (+ (y p1) (y p2))])
  ([p1 p2 p3]
   [(x p1) (+ (y p1) (y p2) (y p3))])
  ([p1 p2 p3 & more]
   [(x p1)
    (apply + (y p1) (y p2) (y p3) (map y more))]))

(defn mul
  ([] zero)
  ([p] p)
  ([p1 p2]
   [(* (x p1) (x p2)) (* (y p1) (y p2))])
  ([p1 p2 p3]
   [(* (x p1) (x p2) (x p3)) (* (y p1) (y p2) (y p3))])
  ([p1 p2 p3 & more]
   [(apply * (x p1) (x p2) (x p3) (map x more))
    (apply * (y p1) (y p2) (y p3) (map y more))]))

(defn mulx
  ([p] p)
  ([p1 p2]
   [(* (x p1) (x p2)) (y p1)])
  ([p1 p2 p3]
   [(* (x p1) (x p2) (x p3)) (y p1)])
  ([p1 p2 p3 & more]
   [(apply * (x p1) (x p2) (x p3) (map x more))
    (y p1)]))

(defn muly
  ([p] p)
  ([p1 p2]
   [(x p1) (* (y p1) (y p2))])
  ([p1 p2 p3]
   [(x p1) (* (y p1) (y p2) (y p3))])
  ([p1 p2 p3 & more]
   [(x p1)
    (apply * (y p1) (y p2) (y p3) (map y more))]))

(defn div
  ([] zero)
  ([p] p)
  ([p1 p2]
   [(/ (x p1) (x p2)) (/ (y p1) (y p2))])
  ([p1 p2 p3]
   [(/ (x p1) (x p2) (x p3)) (/ (y p1) (y p2) (y p3))])
  ([p1 p2 p3 & more]
   [(apply / (x p1) (x p2) (x p3) (map x more))
    (apply / (y p1) (y p2) (y p3) (map y more))]))

(defn divx
  ([p] p)
  ([p1 p2]
   [(/ (x p1) (x p2)) (y p1)])
  ([p1 p2 p3]
   [(/ (x p1) (x p2) (x p3)) (y p1)])
  ([p1 p2 p3 & more]
   [(apply / (x p1) (x p2) (x p3) (map x more))
    (y p1)]))

(defn divy
  ([p] p)
  ([p1 p2]
   [(x p1) (/ (y p1) (y p2))])
  ([p1 p2 p3]
   [(x p1) (/ (y p1) (y p2) (y p3))])
  ([p1 p2 p3 & more]
   [(x p1)
    (apply / (y p1) (y p2) (y p3) (map y more))]))

(defn adj
  ([p]
   (adj (x p) (y p)))
  ([x y]
   (let [x (long x)
         y (long y)]
     [[x (dec y)]
      [(inc x) (dec y)]
      [(inc x) y]
      [(inc x) (inc y)]
      [x (inc y)]
      [(dec x) (inc y)]
      [(dec x) y]
      [(dec x) (dec y)]])))

(defn cadj
  ([p]
   (cadj (x p) (y p)))
  ([x y]
   (let [x (long x)
         y (long y)]
     [[x (dec y)]
      [(inc x) y]
      [x (inc y)]
      [(dec x) y]])))
