(ns falx.flow.ai
  (:require [falx.game :as game]
            [falx.ai :as ai]
            [clojure.core.async :as async :refer [<! >! go-loop go]]
            [falx.world :as world]
            [clojure.tools.logging :refer [error]]
            [falx.creature :as creature]
            [falx.request :as request]))

(defn get-spawn-chan
  [game]
  (game/subxf
    game
    (keep (comp request/spawn-ai :actor))
    [:world.event/actor-created :actor.type/creature]))

(defn get-tick-chan
  [game]
  (async/merge
    [(game/subxf game
                 (map (comp request/tick-ai :actor))
                 :request/spawn-ai)
     (let [c (async/chan)
           complete (game/sub game :ai.event/tick-complete)]
       (go-loop
         []
         (if-some [{:keys [actor timeout]} (<! complete)]
           (do (go (when timeout
                     (<! (async/timeout timeout)))
                   (>! c (request/tick-ai actor)))
               (recur))
           (async/close! c)))
       c)]))

(defn get-messages-chan
  [game]
  (game/subxf game
              (mapcat (fn [{:keys [actor]}]
                        (let [w (game/get-world game)
                              id (:id actor)]
                          ;;relookup the actor so it is the same as
                          ;;that in the world
                          (try
                            (ai/tick w (world/get-actor w id))
                            (catch Throwable e
                              (error e "AI Tick error!")
                              {:type :ai.event/tick-complete
                               :timeout 5000
                               :actor actor})))))
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
        (game/update-actor! game (:id actor) creature/give-goal goal)))))