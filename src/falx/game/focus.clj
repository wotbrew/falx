(ns falx.game.focus
  (:require [falx.game.camera :as camera]
            [falx.world :as world]))

(defn get-all
  "Get all focused things (those things under the mouse)"
  [game]
  (let [point (camera/get-world-mouse-level-point game)
        world (:world game)]
    (world/get-things-by-value world :point point)))

