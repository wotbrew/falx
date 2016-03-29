(ns falx.ui.game
  (:require [falx.ui.game.right :as right]
            [falx.ui.game.bottom :as bottom]
            [falx.ui.game.viewport :as viewport]))


(defn get-actors
  [g sw sh]
  (concat
    (right/get-actors g sw sh)
    (bottom/get-actors g sw sh)
    (viewport/get-actors g sw sh)))
