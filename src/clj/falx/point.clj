(ns falx.point)

(defn point?
  [x]
  (and (vector? x) (= (count x) 2)))