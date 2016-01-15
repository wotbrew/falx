(ns falx.game.selection
  (:require [falx.thing :as thing]
            [falx.thing.creature :as creature]
            [falx.game.time :as time]
            [falx.world :as world]
            [falx.game :as game]
            [falx.game.focus :as focus]))

(defn just-select
  "Selects the given thing, does not check whether selection should be possible.
  - returns the selected thing."
  [thing]
  (if (:selected? thing)
    thing
    (-> (assoc thing :selected? true)
        (thing/publish-event
          {:type  :event.thing/selected
           :thing thing}))))

(defn can-select?
  "Is it possible to select the given thing at the current time?"
  [thing time]
  (and (creature/creature? thing)
       (:player? thing)
       (time/can-act? time (:id thing))))

(defn select
  "Select the thing if possible at current time. Returns the thing."
  [thing time]
  (if (can-select? thing time)
    (just-select thing)
    thing))

(defn unselect
  "Unselects the thing, returns the unselected thing."
  [thing]
  (if-not (:selected? thing)
    thing
    (-> (dissoc thing :selected?)
        (thing/publish-event
          {:type  :event.thing/unselected
           :thing thing}))))

(defn toggle
  "Toggles selection state of the thing"
  [thing time]
  (if (:selected? thing)
    (unselect thing)
    (select thing time)))

(defn get-selected
  "Returns the selected things in the given world"
  [world]
  (world/get-things-by-value world :selected? true))

(defn get-selected-in-level
  "Returns the selected things in the given world & level"
  [world level]
  (filter #(thing/in-level? % level) (get-selected world)))

(defn toggle-in-world
  "Toggles the selection of the thing in the world"
  [world time thing]
  (let [selected (toggle thing time)]
    (world/add-thing world selected)))

(defn toggle-in-game
  "Toggles the selection of the thing in the game"
  [game thing]
  (game/update-world game toggle-in-world (:time game) thing))

(defn select-in-world-exclusive
  "Selects the thing in the world,
  all other things will be unselected."
  [world time thing]
  (let [ethings (get-selected world)
        ethings-to-remove (thing/coll-remove thing ethings)
        selected (select thing time)]
    (->> (map unselect ethings-to-remove)
         (cons selected)
         (world/add-things world))))

(defn select-in-game-exclusive
  "Selects the thing in the game,
  all other things will be unselected."
  [game thing]
  (game/update-world game select-in-world-exclusive (:time game) thing))

(defn get-focused-selectable
  "Returns the focused thing that is selectable."
  [game]
  (let [time (:time game)
        creature (focus/get-creature game)]
    (when (can-select? creature time)
      creature)))

(game/defreaction
  [:event.action :action.hit/select]
  ::select
  (fn [game _]
    (if-some [thing (get-focused-selectable game)]
      (if (game/input-modified? game)
        (toggle-in-game game thing)
        (select-in-game-exclusive game  thing))
      game)))

(game/defreaction
  :event.thing/put
  ::put
  (fn [game {:keys [thing]}]
    (if (:selected? thing)
      (game/publish-event game {:type :event.selection/put
                                :thing thing})
      game)))

(game/defreaction
  :event.thing/unput
  ::unput
  (fn [game {:keys [thing]}]
    (if (:selected? thing)
      (game/publish-event game {:type :event.selection/unput
                                :thing thing})
      game)))

(game/defreaction
  :event.thing/selected
  ::selected
  (fn [game _]
    (game/publish-event game {:type :event.selection/changed})))

(game/defreaction
  :event.thing/unselected
  ::selected
  (fn [game _]
    (game/publish-event game {:type :event.selection/changed})))