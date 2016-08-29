(ns falx.geom
  (:import (falx Point)
           (java.io Writer)))

(defn point
  [x y]
  (Point. x y))

(defmethod print-method Point
  [o ^Writer writer]
  (.write writer (str "#falx.Point" (vec o))))

(defmethod print-dup Point
  [o writer]
  (print-method o writer))