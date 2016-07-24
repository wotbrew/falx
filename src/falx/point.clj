(ns falx.point
  "Functions on points, points are clojure vectors of 2 numeric x,y components.")

(def id
  "The identity vector / zero"
  [0 0])

(defn x
  "Returns the x element of the point"
  [pt]
  (nth pt 0))

(defn y
  "Returns the y element of the point"
  [pt]
  (nth pt 1))

(defn point
  "Coerces the coll into a point"
  [coll]
  (let [[x y] coll]
    [(long x) (long y)]))

(defn add
  "Adds 2 points together"
  ([] id)
  ([pt] pt)
  ([pt1 pt2]
   (add pt1 (x pt2) (y pt2)))
  ([pt x y]
   (add (falx.point/x pt) (falx.point/y pt) x y))
  ([x1 y1 x2 y2]
   [(long (+ x1 x2))
    (long (+ y1 y2))]))

(defn sub
  "Subtracts one point from another"
  ([pt]
   [(- (x pt)) (- (y pt))])
  ([pt1 pt2]
   (sub pt1 (x pt2) (y pt2)))
  ([pt x y]
   (sub (falx.point/x pt) (falx.point/y pt) x y))
  ([x1 y1 x2 y2]
   [(long (- x1 x2))
    (long (- y1 y2))]))

(defn mult
  "Multiplies one point by another"
  ([] id)
  ([pt] pt)
  ([pt1 pt2]
   (mult pt1 (falx.point/x pt2) (falx.point/y pt2)))
  ([pt x y]
   (mult (falx.point/x pt) (falx.point/y pt) x y))
  ([x1 y1 x2 y2]
   [(long (* x1 x2))
    (long (* y1 y2))]))

(defn div
  "Divides one point by another"
  ([pt1 pt2]
   (div pt1 (x pt2) (y pt2)))
  ([pt x y]
   (div (falx.point/x pt) (falx.point/y pt) x y))
  ([x1 y1 x2 y2]
   [(long (/ x1 x2))
    (long (/ y1 y2))]))

(defn shift
  "Adds `n` to each component of the point."
  ([pt n]
   (add pt n n))
  ([x1 y1 n]
   (add x1 y1 n n)))

(defn unshift
  "Subtracts `n` from each component of the point."
  ([pt n]
   (sub pt n n))
  ([x1 y1 n]
   (sub x1 y1 n n)))

(defn scale
  "Scales the point by a factor of `n`. Coerces components to longs"
  ([pt n]
   (mult pt n n))
  ([x y n]
   (mult x y n n)))

(defn shrink
  "Shrinks the point by a factor of `n`. Coerces components to longs"
  ([pt n]
   (div pt n n))
  ([x y n]
   (div x y n n)))

(def north
  "The `north` identity point"
  [0 -1])

(def north-west
  "The `north-west` identity point"
  [-1 -1])

(def north-east
  "The `north-east` identity point"
  [1 -1])

(def west
  "The `west` identity point"
  [-1 0])

(def east
  "The `east` identity point"
  [1 0])

(def south
  "The `south` identity point"
  [0 1])

(def south-east
  "The `south-east` identity point"
  [1 1])

(def south-west
  "The `south-west` identity point"
  [-1 1])

(defn magnitude
  "Returns the magnitude of the point as a double"
  ([pt]
   (let [x (double (x pt))
         y (double (y pt))]
     (Math/sqrt (+ (* x x) (* y y)))))
  ([x y]
   (Math/sqrt (+ (* x x) (* y y)))))

(defn unit
  "Defines the unit point for `pt`"
  ([pt]
   (unit (x pt) (y pt)))
  ([x y]
   (let [x (double x)
         y (double y)
         m (magnitude x y)]
     [(Math/round (/ x m))
      (Math/round (/ y m))])))

(defn opposite
  ([pt]
   (opposite (x pt) (y pt)))
  ([x y]
   (scale x y -1)))

(defn direction
  ([pt1 pt2]
   (direction pt1 (x pt2) (y pt2)))
  ([pt x y]
   (direction (falx.point/x pt) (falx.point/y pt) x y))
  ([x1 y1 x2 y2]
   (unit (sub x2 y2 x1 y1))))

(def clockwise-directions
  [north
   north-east
   east
   south-east
   south
   south-west
   west
   north-west])

(def anti-clockwise-directions
  [north
   north-west
   west
   south-west
   south
   south-east
   east
   north-east])

(def clockwise-cardinal-directions
  [north
   east
   south
   west])

(def anti-clockwise-cardinal-directions
  [north
   west
   south
   east])

(def directions
  clockwise-directions)

(def cardinal-directions
  clockwise-cardinal-directions)

(defn adj
  ([pt]
   (adj (x pt) (y pt)))
  ([x y]
   (let [arr (object-array 8)
         x (long x)
         y (long y)
         x-1 (dec x)
         x+1 (inc x)
         y+1 (inc y)
         y-1 (dec y)]
     (aset arr 0 [x y-1])
     (aset arr 1 [x+1 y-1])
     (aset arr 2 [x+1 y])
     (aset arr 3 [x+1 y+1])
     (aset arr 4 [x y+1])
     (aset arr 5 [x-1 y+1])
     (aset arr 6 [x-1 y])
     (aset arr 7 [x-1 y-1])
     (vec arr))))

(defn adjc
  ([pt]
   (adjc (x pt) (y pt)))
  ([x y]
   (let [arr (object-array 4)
         x (long x)
         y (long y)
         x-1 (dec x)
         x+1 (inc x)
         y+1 (inc y)
         y-1 (dec y)]
     (aset arr 0 [x y-1])
     (aset arr 1 [x+1 y])
     (aset arr 2 [x y+1])
     (aset arr 3 [x-1 y])
     (vec arr))))

(defn in?
  ([pt rect]
    (let [[x y w h] rect]
      (in? pt x y w h)))
  ([pt x2 y2 w2 h2]
    (in? (x pt) (y pt) x2 y2 w2 h2))
  ([x y x2 y2 w2 h2]
   (and
     (<= x2 x (+ x2 w2))
     (<= y2 y (+ y2 h2)))))