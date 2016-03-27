(ns falx.flow.util
  (:require [falx.game :as game]
            [clojure.core.async :as async :refer [go go-loop >! <!]]))

(defn get-in-ms-chan
  [game]
  (let [c (game/sub game :request/in-ms)
        out (async/chan)]
    (go-loop
      []
      (if-some [x (<! c)]
        (do
          (go
            (<! (async/timeout (:ms x 1000)))
            (>! out (:message x)))
          (recur))
        (async/close! out)))
    out))

(defn get-flatten-multi-chan
  [game]
  (game/subxf game
              (mapcat :events)
              :event/multi))

(defn install!
  [game]
  (doto game
    (game/plug!
      (get-in-ms-chan game)
      (get-flatten-multi-chan game))))