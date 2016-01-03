(ns falx.game.move
  (:require [falx.game :as game]
            [falx.thing :as thing]
            [falx.game.selection :as selection]
            [falx.game.focus :as focus]
            [falx.game.goal :as goal]))

(defn teleport-selected
  [game point]
  (let [world (:world game)
        selected (selection/get-selected world)
        stepped (map #(thing/step % point) selected)]
    (game/add-things game stepped)))

(defn move-selected
  [game point]
  (let [world (:world game)
        selected (selection/get-selected world)
        goal (goal/move-to-point point)]
    (->> selected
         (map #(goal/give-exclusive % goal))
         (game/add-things game))))

(game/defreaction
  [:event.action :action.hit/move]
  ::move
  (fn [game _]
    (move-selected game (focus/get-point game))))