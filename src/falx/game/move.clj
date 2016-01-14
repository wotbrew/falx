(ns falx.game.move
  (:require [falx.game :as game]
            [falx.game.selection :as selection]
            [falx.game.focus :as focus]
            [falx.game.goal :as goal]
            [falx.location :as location]
            [falx.thing :as thing]
            [falx.game.solid :as solid]
            [falx.world :as world]))


(defn move-goal
  "Returns a move to cell goal."
  [cell]
  {:type :goal/move
   :cell cell})

(defn can-move?
  [thing cell world]
  (or (not (:solid? thing))
      (not (solid/solid-cell? world cell))))

(defn move
  [thing cell]
  (goal/give-exclusive thing (move-goal cell)))

(game/defreaction
  [:event.action :action.hit/move]
  ::move
  (fn [game _]
    (let [{:keys [world level]} game
          point (focus/get-point game)
          cell (location/cell level point)]
      (->> (selection/get-selected world)
           (filter #(can-move? % cell world))
           (map #(move % cell))
           (game/add-things game)))))

(thing/defreaction
  :event.thing/put
  ::thing-put-complete-move
  (fn [thing {:keys [cell]}]
    (let [goal (move-goal cell)]
      (if (goal/has? thing goal)
        (goal/complete thing goal)
        thing))))

(defn step-goal
  "Return a step goal"
  [cell]
  {:type :goal/step
   :cell cell})

(thing/defreaction
  :event.thing/put
  ::thing-put-complete-step
  (fn [thing {:keys [cell]}]
    (let [goal (step-goal cell)]
      (if (goal/has? thing goal)
        (goal/complete thing goal)
        thing))))

(defn can-step?
  [thing cell world]
  (and (can-move? thing cell world)
       (thing/adjacent-to-cell? thing cell)))

(world/defreaction
  [:event.thing/goal-added :goal/step]
  ::thing-step-goal-added
  (fn [world {:keys [thing goal]}]
    (let [id (:id thing)
          cell (:cell goal)
          thing (world/get-thing world id)]
      (if (can-step? thing cell world)
        (world/update-thing world id thing/step-at-cell cell)
        (world/update-thing world id goal/fail goal)))))

(defn step
  [thing cell]
  (goal/give-exclusive thing (step-goal cell)))