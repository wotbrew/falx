(ns falx.game.debug
  (:require [clojure.tools.logging :refer [debug]]
            [clojure.core.async :as async]))

(defn print-event!
  [event]
  (when (not (:silent? event))
    (debug "--------------------")
    (debug "Event:" (:type event))
    (debug "  " event)))

(defn install!
  [game]
  (debug "Installing debug module")
  (let [event-mult (:event-mult game)
        c (async/chan)]
    (async/tap event-mult c)
    (async/go-loop
      []
      (when-some [x (async/<! c)]
        (print-event! x)
        (recur)))
    game))