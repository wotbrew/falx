(ns falx.game.selection
  (:require [falx.world :as world]))

(defn select
  [creature]
  (assoc creature :selected? true))

(defn unselect
  [creature]
  (dissoc creature :selected?))

(defn get-selected
  [world]
  (world/get-entities-with world :selected? true))

(defn select-in-world
  [world creature]
  (-> (world/add-entity world (select creature))
      (world/publish-event {:type :creature-selected
                            :creature creature})))

(defmethod world/act :select-creature
  [world {:keys [creature]}]
  (select-in-world world creature))

(defmethod world/act :select-creature-only
  [world {:keys [creature]}]
  (let [selected (get-selected world)]
    (-> (world/add-entities world (map unselect selected))
        (select-in-world creature))))