(ns falx.point)

(def zero
  [0 0])

(defn add
  ([] zero)
  ([p1] p1)
  ([p1 p2]
   (mapv +' p1 p2))
  ([p1 p2 & more]
   (apply mapv +' p1 p2 more)))

(defn shift
  [p n]
  (add p [n n]))

(defn mult
  ([] zero)
  ([p1] p1)
  ([p1 p2]
   (mapv *' p1 p2))
  ([p1 p2 & more]
   (apply mapv *' p1 p2 more)))

(defn scale
  [p n]
  (mult p [n n]))

(defn sub
  ([] zero)
  ([p1]
   (mapv -' p1))
  ([p1 p2]
   (mapv -' p1 p2))
  ([p1 p2 & more]
   (apply mapv -' p1 p2 more)))

(defn lshift
  [p n]
  (sub p [n n]))

(defn line-right
  ([n]
   (map vector (range n) (repeat 0)))
  ([p n]
   (map add (repeat p) (line-right n))))

(defn line-left
  ([n]
   (map vector (range 0 (- n) -1) (repeat 0)))
  ([p n]
   (map sub (repeat p) (line-left n))))

(defn line-down
  ([n]
   (map vector (repeat 0) (range n)))
  ([p n]
   (map add (repeat p) (line-down n))))

(defn line-up
  ([n]
   (map vector (repeat 0) (range 0 (- n) -1)))
  ([p n]
   (map sub (repeat p) (line-up n))))