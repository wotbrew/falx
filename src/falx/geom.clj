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