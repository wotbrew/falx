(ns falx.engine.size)

(def id
  [0 0])

(defn w
  [size]
  (nth size 0))

(defn h
  [size]
  (nth size 1))

(defn center
  "Finds the center rect of `size` in rect"
  ([size rect]
   (let [[x2 y2 w2 h2] rect]
     (center size x2 y2 w2 h2)))
  ([size x2 y2 w2 h2]
   (center (w size) (h size) x2 y2 w2 h2))
  ([w1 h1 x2 y2 w2 h2]
   (let [nw (min w1 w2)
         nh (min h1 h2)
         wdiff (- w2 nw)
         hdiff (- h2 nh)]
     [(long (+ x2 (/ wdiff 2)))
      (long (+ y2 (/ hdiff 2)))
      nw
      nh])))

(defn maxw
  ([size1 size2]
   (max-key w size1 size2)))

(defn maxh
  ([size1 size2]
   (max-key h size1 size2)))