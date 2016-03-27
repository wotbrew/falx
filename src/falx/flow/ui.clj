(ns falx.flow.ui
  (:require [falx.game :as game]
            [falx.ui :as ui]))

(defn get-select-request-chan
  [game]
  (game/subxf
    game
    (mapcat (comp #'ui/get-actor-click-messages :actor))
    [:event/actor-clicked :left]))

(defn get-move-goal-chan
  [game]
  (game/subxf
    game
    (mapcat (fn [{:keys [cell]}]
              (ui/get-world-click-messages (game/get-world game) cell)))
    [:event/world-clicked :left]))

(defn install!
  [game]
  (doto game
    (game/plug! (get-select-request-chan game)
                (get-move-goal-chan game))

    (game/subfn!
      :request/toggle-actor-selection
      #(game/update-actor! game (:id (:actor %)) ui/toggle-actor-selection))))