(ns falx.game.move
  (:require [falx.game :as game]
            [falx.game.selection :as selection]
            [falx.game.focus :as focus]
            [falx.game.goal :as goal]
            [falx.location :as location]
            [falx.thing :as thing]))

(defn move-goal
  "Returns a move to cell goal."
  [cell]
  {:type :goal/move-to-cell
   :cell cell})

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
           (map #(move % cell))
           (game/add-things game)))))

(thing/defreaction
  :event.thing/put
  ::thing-put
  (fn [thing {:keys [cell]}]
    (let [goal (move-goal cell)]
      (if (goal/has? thing goal)
        (goal/complete thing goal)
        thing))))