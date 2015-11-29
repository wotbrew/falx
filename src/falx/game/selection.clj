(ns falx.game.selection
  (:require [falx.world :as world]
            [falx.creature :as creature]
            [falx.time :as time]))

(defn can-select?
  [creature time]
  (and (creature/concious? creature)
       (:selectable? creature)
       (or (time/peace? time)
           (time/can-act? time (:id creature)))))

(defn select
  [creature time]
  (if (can-select? creature time)
    (creature/select creature)
    creature))

(defn list-selected
  [world]
  (world/list-with world :selected? true))

(defn list-selected-ids
  [world]
  (world/list-ids-with world :selected? true))