(ns falx.game.move
  (:require [falx.game :as game]
            [falx.game.selection :as selection]
            [falx.game.focus :as focus]
            [falx.game.goal :as goal]
            [falx.thing :as thing]))

(defn move
  [thing cell]
  (goal/give-exclusive thing (goal/move-to-cell cell)))

(defn moving-to?
  [thing cell]
  (goal/has? thing (goal/move-to-cell cell)))

(game/defreaction
  [:event.action :action.hit/move]
  ::move
  (fn [game _]
    (let [{:keys [world level]} game
          point (focus/get-point game)
          cell (thing/cell level point)]
      (->> (selection/get-selected world)
           (map #(move % cell))
           (game/add-things game)))))