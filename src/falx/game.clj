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

(defn run-frame
  [game input frame]
  (let [{:keys [screen world]} game
        actions (screen/get-input-actions screen world input frame)]
    (run-actions game actions)))

(defn get-entity
  [game eid]
  (world/get-entity (:world game) eid))

(defn update-entity
  ([game eid f]
    (update game :world world/update-entity eid f))
  ([game eid f & args]
    (update-entity game eid #(apply f % args))))