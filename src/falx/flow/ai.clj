(ns falx.flow.ai
  (:require [falx.game :as game]
            [falx.ai :as ai]
            [falx.request :as request]
            [clojure.core.async :as async :refer [<! >! go-loop go]]
            [falx.world :as world]
            [clojure.tools.logging :refer [error]]
            [falx.actor :as actor]))

(defn get-spawn-chan
  [game]
  (game/subxf
    game
    (keep (comp request/spawn-ai :actor))
    [:event/actor-created :actor.type/creature]))

(defn get-tick-chan
  [game]
  (game/subxf game (map (fn [event]
                        (request/tick-ai (:actor event) event)))
            :event/actor-goal-added
            :event/actor-goal-removed
            :event/actor-stepped))

(defn get-messages-chan
  [game]
  (game/subxf game
              (mapcat (fn [{:keys [actor event]}]
                        (await (:world-agent game))
                        (let [w (game/get-world game)
                              id (:id actor)
                              actor (world/get-actor w id)]
                          (try
                            (ai/tick w actor event)
                            (catch Throwable e
                              (error e "AI Tick error!"))))))

              :request/tick-ai))

(defn install!
  [game]
  (doto game
    (game/plug! (get-spawn-chan game)
                (get-tick-chan game)
                (get-messages-chan game))

    (game/subfn!
      :request/give-goal
      (fn [{:keys [actor goal]}]
        (game/update-actor! game (:id actor) actor/give-goal goal)))

    (game/subfn!
      :request/remove-goal
      (fn [{:keys [actor goal]}]
        (game/update-actor! game (:id actor) actor/remove-goal goal)))

    (game/subfn!
      :request/step
      (fn [{:keys [actor cell]}]
        (game/update-actor! game (:id actor) actor/step cell)))))