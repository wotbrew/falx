(ns falx.ui
  (:require [falx.game :as g]))

(defn get-all-actor-ids
  [g]
  (g/query g :ui-root? true))

(defn remove-ui
  [g]
  (reduce g/rem-actor g (get-all-actor-ids g)))

(defn relative-coll
  ([pt elements]
    (let [[x y] pt]
      (relative-coll x y elements)))
  ([x y elements]
   (for [e elements]
     (if (:rect e)
       (let [[x2 y2 w h] (:rect e)]
         (assoc e :rect [(+ x x2) (+ y y2) w h]))
       e))))
