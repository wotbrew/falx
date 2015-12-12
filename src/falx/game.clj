(ns falx.game
  (:require [falx.levels.testing :as level-testing]
            [falx.screens.main]
            [falx.screen :as screen]
            [falx.world :as world]))

(def default
  {:world @level-testing/world
   :screen falx.screens.main/default
   :settings {}})

(defn act
  [game action]
  (let [{:keys [screen world]} game]
    (assoc game
      :screen (screen/act screen action)
      :world (world/act world action))))

(defn run-actions
  [game actions]
  (reduce act game actions))