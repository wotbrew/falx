(ns falx.draw.world
  (:require [falx.draw.thing :as draw-thing]
            [falx.world :as world]))

(def cell-width 32)

(def cell-height 32)

(defn draw-slice!
  [world slice]
  (let [things (world/get-things-by-value world :slice slice)]
    (doseq [thing things
            :let [point (:point thing)
                  [x y] point]]
      (draw-thing/draw! thing
                        (* x cell-width)
                        (* y cell-height)
                        cell-width
                        cell-height))))

(def layers
  [:floor
   :wall
   :decor
   :item
   :object
   :creature
   :unknown])

(defn get-slices
  [level]
  (for [layer layers]
    {:layer layer
     :level level}))

(defn draw-level!
  [world level]
  (run! #(draw-slice! world %) (get-slices level)))