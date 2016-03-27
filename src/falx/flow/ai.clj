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
  (game/subxf game
              (map (comp request/tick-ai :actor))
              :request/spawn-ai))

(defn get-messages-chan
  [game]
  (game/subxf game
              (mapcat (fn [{:keys [actor]}]
                        (let [w (game/get-world game)
                              id (:id actor)]
                          ;;relookup the actor so it is the same as
                          ;;that in the world
                          (try
                            (cons
                              (request/tick-ai actor 1000)
                              (ai/tick w (world/get-actor w id)))
                            (catch Throwable e
                              (error e "AI Tick error!")
                              [(request/tick-ai actor 5000)])))))
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
        (game/update-actor! game (:id actor) actor/give-goal goal)))))