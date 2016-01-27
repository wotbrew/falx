(ns falx.position
  (:require [falx.point :as point]))

(defn cell
  [point level]
  {:point point
   :level level})

(defn adjacent?
  [c1 c2]
  (and (= (:level c1) (:level c2))
       (point/adjacent? (:point c1) (:point c2))))

(defn slice
  [layer level]
  {:layer layer
   :level level})