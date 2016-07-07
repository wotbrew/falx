(ns falx.geom)

(def ^:private point-id {::x 0 ::y 0})
(def ^:private size-id {::w 0 ::h 0})
(def ^:private rect-id {::x 0 ::y 0 ::w 0 ::h 0})

(defn point
  ([]
   point-id)
  ([g]
   (not-empty (select-keys g [::x ::y])))
  ([x y]
   {::x x ::y y}))

(defn size
  ([]
   size-id)
  ([g]
   (not-empty (select-keys g [::w ::h])))
  ([w h]
   {::w w ::h h}))

(defn rect
  ([]
   rect-id)
  ([g]
   (when (and (::x g) (::y g) (::w g) (::h g))
     (select-keys g [::x ::y ::w ::h])))
  ([x y w h]
   {::x x ::y y ::w w ::h h}))

(defn add
  ([g1]
   g1)
  ([g1 g2]
   (merge-with +' g1 g2))
  ([g1 g2 & more]
   (reduce add (add g1 g2) more)))

(defn sub
  ([g1]
   g1)
  ([g1 g2]
   (merge-with -' g1 g2))
  ([g1 g2 & more]
   (reduce sub (sub g1 g2) more)))

(defn mult
  ([g1]
   g1)
  ([g1 g2]
   (merge-with *' g1 g2))
  ([g1 g2 & more]
   (reduce sub (sub g1 g2) more)))

(defn put
  ([g1 g2]
   (merge g1 (point g2)))
  ([g x y]
   (assoc g ::x x ::y y)))

(defn resize
  ([g1 g2]
   (merge g1 (size g2)))
  ([g w h]
   (assoc g ::w w ::h h)))

(defn points
  [g]
  (let [x (::x g 0)
        y (::y g 0)
        w (::w g 1)
        h (::h g 1)
        xw (+ x w)
        yh (+ y h)]
    (for [x (range x xw)
          y (range y yh)]
      (point x y))))

(defn ->rect-tuple
  [g]
  [(::x g 0) (::y g 0) (::w g 0) (::h g 0)])

(defn rect-tuple->geom
  [[x y w h]]
  {::x (or x 0)
   ::y (or y 0)
   ::w (or w 0)
   ::h (or h 0)})

(defn ->size-tuple
  [g]
  [(::w g 0) (::h g 0)])

(defn size-tuple->geom
  [[w h]]
  {::w (or w 0)
   ::h (or h 0)})

(defn ->point-tuple
  [g]
  [(::x g 0) (::y g 0)])

(defn point-tuple->geom
  [[x y]]
  {::x (or x 0)
   ::y (or y 0)})

(def north
  (point 0 -1))

(def north-east
  (point 1 -1))

(def east
  (point 1 0))

(def south-east
  (point 1 1))

(def south
  (point 0 1))

(def south-west
  (point -1 1))

(def west
  (point -1 0))

(def north-west
  (point -1 -1))