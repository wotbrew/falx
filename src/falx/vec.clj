(ns falx.vec
  "Functions on 2d vectors, revresented as a clojure vector of doubles."
  (:refer-clojure :exclude [vec]))

(def id
  [0.0 0.0])

(defn x
  "Returns the x element of the vec"
  [v]
  (nth v 0))

(defn y
  "Returns the y element of the vec"
  [v]
  (nth v 1))

(defn vec
  "Coerces the coll into a vec"
  [coll]
  (let [[x y] coll]
    [(double x) (double y)]))

(defn add
  "Adds 2 vecs together"
  ([] id)
  ([v] v)
  ([v1 v2]
   (add v1 (x v2) (y v2)))
  ([v x y]
   (add (falx.vec/x v) (falx.vec/y v) x y))
  ([x1 y1 x2 y2]
   [(double (+ x1 x2))
    (double (+ y1 y2))]))

(defn sub
  "Subtracts one vec from another"
  ([v]
   [(- (x v)) (- (y v))])
  ([v1 v2]
   (sub v1 (x v2) (y v2)))
  ([v x y]
   (sub (falx.vec/x v) (falx.vec/y v) x y))
  ([x1 y1 x2 y2]
   [(double (- x1 x2))
    (double (- y1 y2))]))

(defn mult
  "Multivlies one vec by another"
  ([] id)
  ([v] v)
  ([v1 v2]
   (mult v1 (falx.vec/x v2) (falx.vec/y v2)))
  ([v x y]
   (mult (falx.vec/x v) (falx.vec/y v) x y))
  ([x1 y1 x2 y2]
   [(double (* x1 x2))
    (double (* y1 y2))]))

(defn div
  "Divides one vec by another"
  ([v1 v2]
   (div v1 (x v2) (y v2)))
  ([v x y]
   (div (falx.vec/x v) (falx.vec/y v) x y))
  ([x1 y1 x2 y2]
   [(double (/ x1 x2))
    (double (/ y1 y2))]))

(defn shift
  "Adds `n` to each comvonent of the vec."
  ([v n]
   (add v n n))
  ([x1 y1 n]
   (add x1 y1 n n)))

(defn unshift
  "Subtracts `n` from each comvonent of the vec."
  ([v n]
   (sub v n n))
  ([x1 y1 n]
   (sub x1 y1 n n)))

(defn scale
  "Scales the vec by a factor of `n`."
  ([v n]
   (mult v n n))
  ([x y n]
   (mult x y n n)))

(defn shrink
  "Shrinks the vec by a factor of `n`."
  ([v n]
   (div v n n))
  ([x y n]
   (div x y n n)))

(defn magnitude
  "Returns the magnitude of the vector as a double"
  ([v]
   (let [x (double (x v))
         y (double (y v))]
     (Math/sqrt (+ (* x x) (* y y)))))
  ([x y]
   (Math/sqrt (+ (* x x) (* y y)))))

(defn unit
  "Defines the unit vector for `v`"
  ([v]
   (unit (x v) (y v)))
  ([x y]
   (shrink x y (magnitude x y))))

(defn opposite
  ([v]
   (opposite (x v) (y v)))
  ([x y]
   (scale x y -1.0)))

(defn direction
  ([v1 v2]
   (direction v1 (x v2) (y v2)))
  ([v x y]
   (direction (falx.vec/x v) (falx.vec/y v) x y))
  ([x1 y1 x2 y2]
   (unit (sub x2 y2 x1 y1))))

(defn dot-product
  "Takes the dot product of the vectors"
  ([v1 v2]
   (dot-product v1 (x v2) (y v2)))
  ([v x y]
   (dot-product (falx.vec/x v) (falx.vec/y v) x y))
  ([x1 y1 x2 y2]
   (+ (* x1 x2)
      (* y1 y2))))

(defn cosin
  ([v1 v2]
   (cosin v1 (x v2) (y v2)))
  ([v x y]
   (cosin (falx.vec/x v) (falx.vec/y v) x y))
  ([x1 y1 x2 y2]
   (double (/ (dot-product x1 y1 x2 y2)
              (* (magnitude x1 y1)
                 (magnitude x2 y2))))))
