(ns falx.game.focus
  (:require [falx.game.camera :as camera]
            [falx.world :as world]
            [falx.thing.creature :as creature]
            [falx.util :as util]
            [falx.location :as location]))

(defn get-point
  "Get the focused point"
  [game]
  (camera/get-world-mouse-level-point game))

(defn get-cell
  "Get the focused cell"
  [game]
  (location/cell (:level game) (get-point game)))

(defn get-all-things
  "Get all focused things (those things under the mouse)"
  [game]
  (let [{:keys [world]} game
        cell (get-cell game)]
    (world/get-things-by-value world :cell cell)))

(defn get-creature
  "Returns the focused creature, nil if none"
  [game]
  (util/find-first creature/creature? (get-all-things game)))