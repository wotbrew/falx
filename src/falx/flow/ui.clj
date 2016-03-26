(ns falx.flow.ui
  (:require [falx.game :as game]
            [falx.creature :as creature]
            [falx.ui :as ui]
            [falx.request :as request]
            [falx.world :as world]))

(defn get-select-request-chan
  [game]
  (game/subxf
    game
    (mapcat (comp #'ui/get-creature-click-requests :actor))
    [:ui.event/actor-clicked :actor.type/creature :left]))

(defn get-move-goal-chan
  [game]
  (game/subxf
    game
    (mapcat (fn [{:keys [cell]}]
              (ui/get-world-click-requests (game/get-world game) cell)))
    [:ui.event/world-clicked :left]))

(defn install!
  [game]
  (doto game
    (game/plug! (get-select-request-chan game)
                (get-move-goal-chan game))

    (game/subfn!
      :request/toggle-creature-selection
      #(game/update-actor! game (:id (:actor %)) ui/toggle-creature-selection))))