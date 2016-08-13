(ns falx.engine.vec
  "Functions on 2d vectors, revresented as a clojure vector of doubles."
  (:refer-clojure :exclude [vec]))

(def id
  [0.0 0.0])

(defn x
  "Returns the x element of the vec"
  [vec]
  (nth vec 0))

(defn y
  "Returns the y element of the vec"
  [vec]
  (nth vec 1))

(defn vec
  "Coerces the coll into a vec"
  [coll]
  (let [[x y] coll]
    [(double x) (double y)]))

(defn add
  "Adds 2 vecs together"
  ([] id)
  ([vec] vec)
  ([vec1 vec2]
   (add vec1 (x vec2) (y vec2)))
  ([vec x y]
   (add (falx.vec/x vec) (falx.vec/y vec) x y))
  ([x1 y1 x2 y2]
   [(double (+ x1 x2))
    (double (+ y1 y2))]))

(defn sub
  "Subtracts one vec from another"
  ([vec]
   [(- (x vec)) (- (y vec))])
  ([vec1 vec2]
   (sub vec1 (x vec2) (y vec2)))
  ([vec x y]
   (sub (falx.vec/x vec) (falx.vec/y vec) x y))
  ([x1 y1 x2 y2]
   [(double (- x1 x2))
    (double (- y1 y2))]))

(defn mult
  "Multivlies one vec by another"
  ([] id)
  ([vec] vec)
  ([vec1 vec2]
   (mult vec1 (falx.vec/x vec2) (falx.vec/y vec2)))
  ([vec x y]
   (mult (falx.vec/x vec) (falx.vec/y vec) x y))
  ([x1 y1 x2 y2]
   [(double (* x1 x2))
    (double (* y1 y2))]))

(defn div
  "Divides one vec by another"
  ([vec1 vec2]
   (div vec1 (x vec2) (y vec2)))
  ([vec x y]
   (div (falx.vec/x vec) (falx.vec/y vec) x y))
  ([x1 y1 x2 y2]
   [(double (/ x1 x2))
    (double (/ y1 y2))]))

(defn shift
  "Adds `n` to each comvonent of the vec."
  ([vec n]
   (add vec n n))
  ([x1 y1 n]
   (add x1 y1 n n)))

(defn unshift
  "Subtracts `n` from each comvonent of the vec."
  ([vec n]
   (sub vec n n))
  ([x1 y1 n]
   (sub x1 y1 n n)))

(defn scale
  "Scales the vec by a factor of `n`."
  ([vec n]
   (mult vec n n))
  ([x y n]
   (mult x y n n)))

(defn shrink
  "Shrinks the vec by a factor of `n`."
  ([vec n]
   (div vec n n))
  ([x y n]
   (div x y n n)))

(defn magnitude
  "Returns the magnitude of the vector as a double"
  ([vec]
   (let [x (double (x vec))
         y (double (y vec))]
     (Math/sqrt (+ (* x x) (* y y)))))
  ([x y]
   (Math/sqrt (+ (* x x) (* y y)))))

(defn unit
  "Defines the unit vector for `vec`"
  ([vec]
   (unit (x vec) (y vec)))
  ([x y]
   (shrink x y (magnitude x y))))

(defn opposite
  ([vec]
   (opposite (x vec) (y vec)))
  ([x y]
   (scale x y -1.0)))

(defn direction
  ([vec1 vec2]
   (direction vec1 (x vec2) (y vec2)))
  ([vec x y]
   (direction (falx.vec/x vec) (falx.vec/y vec) x y))
  ([x1 y1 x2 y2]
   (unit (sub x2 y2 x1 y1))))

(defn dot-product
  "Takes the dot product of the vectors"
  ([vec1 vec2]
   (dot-product vec1 (x vec2) (y vec2)))
  ([vec x y]
   (dot-product (falx.vec/x vec) (falx.vec/y vec) x y))
  ([x1 y1 x2 y2]
   (+ (* x1 x2)
      (* y1 y2))))

(defn cosin
  ([vec1 vec2]
   (cosin vec1 (x vec2) (y vec2)))
  ([vec x y]
   (cosin (falx.vec/x vec) (falx.vec/y vec) x y))
  ([x1 y1 x2 y2]
   (double (/ (dot-product x1 y1 x2 y2)
              (* (magnitude x1 y1)
                 (magnitude x2 y2))))))
