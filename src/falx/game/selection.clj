(ns falx.game.selection
  (:require [falx.thing :as thing]
            [falx.thing.creature :as creature]
            [falx.game.time :as time]
            [falx.world :as world]))

(defn just-select
  [thing]
  (if (:selected? thing)
    thing
    (-> (assoc thing :selected? true)
        (thing/publish-event
          {:type  :event.thing/selected
           :thing thing}))))

(defn can-select?
  [thing time]
  (and (creature/creature? thing)
       (time/can-act? time (:id thing))))

(defn select
  [thing time]
  (if (can-select? thing time)
    (just-select thing)
    thing))

(defn unselect
  [thing]
  (if-not (:selected? thing)
    thing
    (-> (dissoc thing :selected?)
        (thing/publish-event
          {:type  :event.thing/unselected
           :thing thing}))))

(defn select-id
  [world id time]
  (world/update-thing world id select time))

(defn unselect-id
  [world id]
  (world/update-thing world id unselect))

(defn unselect-but-id
  [world id]
  (let [ids (world/get-ids-by-value world :selected? true)]
    (reduce unselect-id world (disj ids id))))

(defn select-ids
  [world ids time]
  (reduce #(select-id %1 %2 time) world ids))

(defn get-selected
  [world]
  (world/get-things-by-value world :selected? true))

(defn get-selected-ids
  [world]
  (world/get-ids-by-value world :selected? true))

(defn selected?
  [thing]
  (:selected? thing))

(defn selected-id?
  [world id]
  (world/get-attribute world id :selected?))