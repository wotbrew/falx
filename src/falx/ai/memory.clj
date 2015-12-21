(ns falx.ai.memory
  (:require [falx.world :as world]
            [falx.state :as state]
            [falx.util :refer :all]))

(defn recall
  [eid]
  (:ai (world/get-entity (:world @state/game) eid)))

(defn remember!
  [eid m]
  (swap! state/game update :world world/update-entity eid update :ai merge m)
  nil)

(defn update!
  ([eid k f]
   (swap! state/game update :world world/update-entity eid update-in [:ai k] f)
    nil)
  ([eid k f & args]
   (update! eid k #(apply f % args))))

(defn forget!
  [eid k]
  (swap! state/game update :world world/update-entity eid dissoc-in [:ai k])
  nil)
