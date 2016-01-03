(ns falx.game.selection
  (:require [falx.thing :as thing]
            [falx.thing.creature :as creature]
            [falx.game.time :as time]
            [falx.world :as world]
            [falx.game :as game]
            [falx.game.focus :as focus]
            [falx.input :as input]
            [clojure.set :as set]))

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

(defn select-in-world
  "Selects the coll of things in the world"
  [world time things]
  (let [selected (map #(toggle % time) things)]
    (world/add-things world selected)))

(defn select-in-game
  "Selects the coll of things in the game"
  [game things]
  (game/update-world game select-in-world (:time game) things))

(defn select-in-world-exclusive
  "Selects the coll of things in the world,
  all other things will be unselected."
  [world time things]
  (let [ethings (get-selected world)
        ethings-to-remove (thing/coll-difference things ethings)
        selected (map #(select % time) things)]
    (->> (map unselect ethings-to-remove)
         (concat selected)
         (world/add-things world))))

(defn select-in-game-exclusive
  "Selects the coll of things in the game,
  all other things will be unselected."
  [game things]
  (game/update-world game select-in-world-exclusive (:time game) things))

(defn get-selectable-focused-things
  "Returns the focused things that are selectable."
  [game]
  (let [ts (focus/get-all-things game)
        time (:time game)]
    (filter #(can-select? % time) ts)))

(game/defreaction
  [:event.action :action.hit/select]
  ::select
  (fn [game _]
    (let [things (get-selectable-focused-things game)]
      (if (empty? things)
        game
        (if (game/input-modified? game)
          (select-in-game game things)
          (select-in-game-exclusive game things))))))