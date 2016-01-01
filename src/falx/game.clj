(ns falx.game
  (:require [falx.world :as world]))

(defn split-events
  [game]
  (let [{:keys [world]} game
        {:keys [events world]} (world/split-events world)]
    {:events events
     :game (assoc game :world world)}))

(defn update-world
  ([game f]
    (update game :world f))
  ([game f & args]
    (update-world game #(apply f % args))))