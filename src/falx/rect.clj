(ns falx.rect)

(def id
  [0 0 0 0])

(defn x
  [rect]
  (nth rect 0))

(defn y
  [rect]
  (nth rect 1))

(defn w
  [rect]
  (nth rect 2))

(defn h
  [rect]
  (nth rect 3))
