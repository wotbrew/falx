(ns falx.game.move
  (:require [falx.game :as game]
            [falx.game.selection :as selection]
            [falx.world :as world]
            [falx.thing :as thing]
            [falx.game.focus :as focus]))

(defn teleport-selected
  [game point]
  (let [world (:world game)
        selected (selection/get-selected world)
        stepped (map #(thing/step % point) selected)]
    (game/add-things game stepped)))

(game/defreaction
  [:event.action :action.hit/move]
  ::move
  (fn [game _]
    (teleport-selected game (focus/get-point game))))