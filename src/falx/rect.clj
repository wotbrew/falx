(ns falx.rect)

(def zero
  [0 0 0 0])

(defn from-point
  ([p [w h]]
   (from-point p w h))
  ([p w h]
   [(nth p 0) (nth p 1) w h]))

(defn point
  [r]
  [(nth r 0)
   (nth r 1)])

(defn size
  [r]
  [(nth r 2)
   (nth r 3)])

(defn put
  [r p]
  (from-point p (nth r 2) (nth r 3)))

(defn add
  ([] zero)
  ([r1] r1)
  ([r1 r2]
   (mapv +' r1 r2))
  ([r1 r2 & more]
   (apply mapv +' r1 r2 more)))

(defn shift
  [r n]
  (add r [n n 0 0]))

(defn mult
  ([] zero)
  ([r1] r1)
  ([r1 r2]
   (mapv *' r1 r2))
  ([r1 r2 & more]
   (apply mapv *' r1 r2 more)))

(defn scale
  ([r n]
   (mult r [n n n n])))

(defn expand
  ([r n]
   (expand r n n))
  ([r w h]
   (mult r [1 1 w h])))

(defn sub
  ([] zero)
  ([r1]
   (mapv -' r1))
  ([r1 r2]
   (mapv -' r1 r2))
  ([r1 r2 & more]
   (apply mapv -' r1 r2 more)))

(defn lshift
  [r n]
  (sub r [n n 0 0]))