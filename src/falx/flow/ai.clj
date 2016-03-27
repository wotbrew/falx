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

(defn get-auto-tick-chan
  [game]
  (let [c (game/sub game :request/spawn-ai)
        out (async/chan 64)]
    (go-loop
      []
      (if-some [x (<! c)]
        (let [actor (:actor x)]
          (go-loop
            []
            (<! (async/timeout 1000))
            (when (>! out (request/tick-ai actor))
              (recur)))
          (recur))
        (async/close! out)))
    out))

(defn get-goal-tick-chan
  [game]
  (game/subxf game (map (comp request/tick-ai :actor))
              :event/actor-goal-added))

(defn get-messages-chan
  [game]
  (game/subxf game
              (mapcat (fn [{:keys [actor]}]
                        (let [w (game/get-world game)
                              id (:id actor)
                              actor (world/get-actor w id)]
                          (try
                            (await (:world-agent game))
                            (ai/tick w actor)
                            (catch Throwable e
                              (error e "AI Tick error!"))))))

              :request/tick-ai))

(defn install!
  [game]
  (doto game
    (game/plug! (get-spawn-chan game)
                (get-auto-tick-chan game)
                (get-goal-tick-chan game)
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