(ns falx.game.select
  (:require [clojure.tools.logging :refer [debug]]
            [falx.creature :as creature]
            [falx.game :as game]
            [falx.world :as world]))

(defn toggle-select
  [world id]
  (world/update-actor world id creature/toggle-select))

(defn get-selected
  [world]
  (world/query-actors world :selected? true))

(defn get-currently-selected
  [game]
  (get-selected (game/get-world game)))

(defn try-select!
  [game actor]
  (let [wa (:world-agent game)
        id (:id actor)]
    (send wa toggle-select id)
    {:type  :select.event/attempted-select
     :id    id
     :actor actor}))

(defn install!
  [game]
  (debug "Installing select module")
  (game/install-event-fn
    game
    [:ui.event/actor-clicked :actor.type/creature]
    #(try-select! game (:actor %)))
  game)