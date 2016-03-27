(ns falx.flow.debug
  (:require [falx.game :as game]
            [falx.io.debug :as debug]
            [clojure.core.async :as async]
            [falx.request :as request]))

(defn get-print-message-chan
  [game]
  (game/sub! game
             (async/chan (async/dropping-buffer 32)
                         (keep (fn [msg]
                                 (when (and (not (:silent? msg))
                                            (not (debug/silent-message? (:type msg))))
                                   (request/print-message msg)))))))

(defn get-print-actor-chan
  [game]
  (game/sub! game
             [:event/actor-clicked :right]
             (async/chan (async/dropping-buffer 32)
                         (map (comp request/print-actor :actor)))))

(defn install!
  [game]
  (doto game
    (game/plug! (get-print-message-chan game)
                (get-print-actor-chan game))

    (game/subfn!
      :request/print-message
      #(debug/message! (:message %)))
    (game/subfn!
      :request/print-actor
      #(debug/actor! (:actor %)))))