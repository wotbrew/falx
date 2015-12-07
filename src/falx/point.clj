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
