(ns falx.point)

(defn line-right
  ([point]
    (let [[x y] point]
      (line-right x y)))
  ([x y]
    (for [x (iterate inc x)]
      [x y])))

(defn line-left
  ([point]
   (let [[x y] point]
     (line-left x y)))
  ([x y]
   (for [x (iterate dec x)]
     [x y])))

(defn line-down
  ([point]
   (let [[x y] point]
     (line-down x y)))
  ([x y]
   (for [y (iterate inc y)]
     [x y])))

(defn line-up
  ([point]
   (let [[x y] point]
     (line-up x y)))
  ([x y]
   (for [y (iterate dec y)]
     [x y])))

(defn add
  ([point1 point2]
    (let [[x2 y2] point2]
      (add point1 x2 y2)))
  ([point x2 y2]
   (let [[x y] point]
     (add x y x2 y2)))
  ([x y x2 y2]
   [(+ x x2)
    (+ y y2)]))