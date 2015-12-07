(ns falx.draw.world
  (:require [falx.draw.entity :as draw-entity]
            [falx.entity :as entity]
            [falx.world :as world]))

(def layers
  [:floor
   :terrain
   :decor
   :object
   :creature
   :misc])

(def cell-width 32)
(def cell-height 32)

(defn draw-eid!
  [world eid]
  (let [entity (world/get-entity world eid)
        [x y] (:point entity)]
    (draw-entity/draw! entity (* x cell-width) (* y cell-height) cell-width cell-height)))

(defn draw-slice!
  [world slice]
  (let [eids (world/get-eids-with world :slice slice)]
    (run! #(draw-eid! world %) eids)))

(defn draw!
  [world level]
  (let [slices (map #(entity/slice level %) layers)]
    (run! #(draw-slice! world %) slices)))