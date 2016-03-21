(ns falx.game.move
  (:require [clojure.tools.logging :refer [debug]]
            [falx.game :as game]
            [falx.game.select :as game-select]
            [falx.actor :as actor]
            [falx.world :as world]))

(defn selected-asked-to-move
  [world cell]
  (let [acoll (world/get-at world cell)]
    (when-some [selected (seq (game-select/get-selected world))]
      (when (not (some #(actor/some-obstructs? % acoll) selected))
        {:type     :move.event/selected-asked-to-move
         :selected selected
         :cell     cell}))))

(defn install!
  [game]
  (debug "Installing move module")
  (game/install-event-fn
    game
    :ui.event/world-clicked
    #(selected-asked-to-move (game/get-world game) (:cell %)))
  game)