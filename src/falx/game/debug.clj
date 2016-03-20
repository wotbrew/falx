(ns falx.game.debug
  (:require [clojure.tools.logging :refer [debug]]
            [clojure.core.async :as async]))

(def silence?
  #{})

(def display-details?
  #{#_[:ui.event/actor-clicked :actor.type/creature]})

(defn print-event!
  [event]
  (let [type (:type event)]
    (when (and (not (:silent? event))
               (not (silence? type)))
      (debug "--------------------")
      (debug "Event:" type)
      (when (display-details? type)
        (debug "  " event)))))

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