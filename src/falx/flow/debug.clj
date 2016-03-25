(ns falx.flow.debug
  (:require [falx.game :as game]
            [falx.io.print :as print]
            [clojure.core.async :as async]))

(defn get-print-message-chan
  [game]
  (game/sub! game
             (async/chan (async/dropping-buffer 32)
                         (keep (fn [msg]
                                 (when (and (not (:silent? msg))
                                            (not (print/silent-message? (:type msg))))
                                   {:type    :request/print-message
                                    :silent? true
                                    :message msg}))))))

(defn get-print-actor-chan
  [game]
  (game/sub! game
             [:ui.event/actor-clicked :right]
             (async/chan (async/dropping-buffer 32)
                         (map (fn [{:keys [actor]}]
                                {:type :request/print-actor
                                 :actor actor})))))

(defn install!
  [game]
  (doto game
    (game/plug! (get-print-message-chan game)
                (get-print-actor-chan game))

    (game/subfn!
      :request/print-message
      #(print/message! (:message %)))
    (game/subfn!
      :request/print-actor
      #(print/actor! (:actor %)))))