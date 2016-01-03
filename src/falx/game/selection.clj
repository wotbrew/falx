(ns falx.game.selection
  (:require [falx.thing :as thing]
            [falx.thing.creature :as creature]
            [falx.game.time :as time]
            [falx.world :as world]
            [falx.game :as game]
            [falx.game.focus :as focus]))

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

(defn toggle
  [thing time]
  (if (:selected? thing)
    (unselect thing)
    (select thing time)))

(game/defreaction!
  [:event.action :action.hit/select]
  ::select
  (fn [game _]
    (let [ts (focus/get-all game)
          time (:time game)
          selectable (filter #(can-select? % time) ts)
          selected (map #(toggle % time) selectable)]
      (game/add-things game selected))))