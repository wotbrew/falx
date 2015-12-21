(ns falx.ai
  (:require [clojure.core.async :as async
             :refer [<! >! chan timeout go go-loop]]
            [falx.ai.path :as path]))

(def tick-rate 100)

(defn tick
  [eid]
  (go
    (<! (path/path-behaviour eid))
    (<! (path/step-behaviour eid))))

(defn ai
  [eid]
  (let [input (chan)]
    (go-loop
      []
      (let [message (async/alt!
                      input ([x] x)
                      (timeout tick-rate) ([x] :tick))]
        (case message
          :tick (do (<! (tick eid))
                    (recur))
          nil nil)))))
