(ns falx.game.focus
  (:require [falx.game.camera :as camera]
            [falx.world :as world]
            [falx.thing.creature :as creature]
            [falx.util :as util]))

(defn get-point
  "Get the focused point"
  [game]
  (camera/get-world-mouse-level-point game))

(defn get-all-things
  "Get all focused things (those things under the mouse)"
  [game]
  (let [point (get-point game)
        world (:world game)]
    (world/get-things-by-value world :point point)))

(defn get-creature
  "Returns the focused creature, nil if none"
  [game]
  (util/find-first creature/creature? (get-all-things game)))