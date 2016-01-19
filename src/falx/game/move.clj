(ns falx.game.move
  (:require [falx.game :as game]
            [falx.game.selection :as selection]
            [falx.game.focus :as focus]
            [falx.game.goal :as goal]
            [falx.location :as location]
            [falx.thing :as thing]
            [falx.game.solid :as solid]
            [falx.world :as world]))

(defn goto-cell-goal
  [cell]
  {:type :goal/goto-cell
   :cell cell})

(defn goto-thing-goal
  [thing]
  {:type :goal/goto-thing
   :thing thing})

(defn goto-thing-within-goal
  [thing distance]
  {:type :goal/goto-thing-within
   :thing thing
   :distance distance})

(defn goto-cell
  [thing cell]
  (goal/give thing (goto-cell-goal cell)))

(defn goto-thing
  [thing target-thing]
  (goal/give thing (goto-thing-goal target-thing)))

(defn goto-thing-within
  [thing distance]
  (goal/give thing (goto-thing-within-goal thing distance)))

(defn can-move?
  [thing cell world]
  (or (not (:solid? thing))
      (not (solid/solid-cell? world cell))))

(defn step
  [world id cell]
  (let [thing (world/get-thing world id)]
    (if (can-move? thing cell world)
      (world/update-thing world id thing/step cell)
      world)))

(defn step-for-goal
  [world id cell goal]
  (if (goal/has? (world/get-thing world id) goal)
    (step world id cell)
    world))

(defn get-nearest-cell
  [world thing target-thing]
  (let [target-cell (:cell target-thing)]
    (thing/get-nearest-cell thing (when target-cell
                                    (->> (location/get-adjacent target-cell)
                                         (filter #(can-move? thing % world)))))))


(game/defreaction
  [:event.action :action.hit/move]
  ::move
  (fn [game _]
    (let [{:keys [world level]} game
          point (focus/get-point game)
          cell (location/cell level point)]
      (->> (selection/get-selected world)
           (filter #(can-move? % cell world))
           (map #(goto-cell % cell))
           (game/add-things game)))))