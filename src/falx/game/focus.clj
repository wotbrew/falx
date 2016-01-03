(ns falx.game.focus
  (:require [falx.game.camera :as camera]
            [falx.world :as world]))

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

