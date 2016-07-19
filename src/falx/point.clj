(ns falx.point
  "Functions on points, points are clojure vectors of 2 numeric x,y components.")

(def id
  "The identity vector / zero"
  [0 0])

(defn x
  "Returns the x element of the point"
  [p]
  (nth p 0))

(defn y
  "Returns the y element of the point"
  [p]
  (nth p 1))

(defn point
  "Coerces the coll into a point"
  [coll]
  (let [[x y] coll]
    [(long x) (long y)]))

(defn add
  "Adds 2 points together"
  ([] id)
  ([p] p)
  ([p1 p2]
   (add p1 (x p2) (y p2)))
  ([p x y]
   (add (falx.point/x p) (falx.point/y p) x y))
  ([x1 y1 x2 y2]
   [(long (+ x1 x2))
    (long (+ y1 y2))]))

(defn sub
  "Subtracts one point from another"
  ([p]
   [(- (x p)) (- (y p))])
  ([p1 p2]
   (sub p1 (x p2) (y p2)))
  ([p x y]
   (sub (falx.point/x p) (falx.point/y p) x y))
  ([x1 y1 x2 y2]
   [(long (- x1 x2))
    (long (- y1 y2))]))

(defn mult
  "Multiplies one point by another"
  ([] id)
  ([p] p)
  ([p1 p2]
   (mult p1 (falx.point/x p2) (falx.point/y p2)))
  ([p x y]
   (mult (falx.point/x p) (falx.point/y p) x y))
  ([x1 y1 x2 y2]
   [(long (* x1 x2))
    (long (* y1 y2))]))

(defn div
  "Divides one point by another"
  ([p1 p2]
   (div p1 (x p2) (y p2)))
  ([p x y]
   (div (falx.point/x p) (falx.point/y p) x y))
  ([x1 y1 x2 y2]
   [(long (/ x1 x2))
    (long (/ y1 y2))]))

(defn shift
  "Adds `n` to each component of the point."
  ([p n]
   (add p n n))
  ([x1 y1 n]
   (add x1 y1 n n)))

(defn unshift
  "Subtracts `n` from each component of the point."
  ([p n]
   (sub p n n))
  ([x1 y1 n]
   (sub x1 y1 n n)))

(defn scale
  "Scales the point by a factor of `n`. Coerces components to longs"
  ([p n]
   (mult p n n))
  ([x y n]
   (mult x y n n)))

(defn shrink
  "Shrinks the point by a factor of `n`. Coerces components to longs"
  ([p n]
   (div p n n))
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
  ([p]
   (let [x (double (x p))
         y (double (y p))]
     (Math/sqrt (+ (* x x) (* y y)))))
  ([x y]
   (Math/sqrt (+ (* x x) (* y y)))))

(defn unit
  "Defines the unit point for `p`"
  ([p]
   (unit (x p) (y p)))
  ([x y]
   (let [x (double x)
         y (double y)
         m (magnitude x y)]
     [(Math/round (/ x m))
      (Math/round (/ y m))])))

(defn opposite
  ([p]
   (opposite (x p) (y p)))
  ([x y]
   (scale x y -1)))

(defn direction
  ([p1 p2]
   (direction p1 (x p2) (y p2)))
  ([p x y]
   (direction (falx.point/x p) (falx.point/y p) x y))
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
  ([p]
   (adj (x p) (y p)))
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
  ([p]
   (adjc (x p) (y p)))
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