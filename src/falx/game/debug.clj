(ns falx.game.debug
  (:require [clojure.tools.logging :refer [debug]]
            [clojure.pprint :as pp]
            [falx.game :as game]))

(def silence?
  #{})

(def display-details?
  #{#_[:ui.event/actor-clicked :actor.type/creature]
    [:creature.event/goal-given :goal.type/move]
    :creature.event/unselected
    :creature.event/selected})

(defn pprint-str
  [o]
  (with-out-str
    (pp/pprint o)))

(defn assoc-some
  [m k v]
  (if (contains? m k)
    (assoc m k v)
    m))

(defn print-event!
  [event]
  (let [type (:type event)]
    (when (and (not (:silent? event))
               (not (silence? type)))
      (debug "Event:" type)
      (when (display-details? type)
        (debug (pprint-str (assoc-some event :world "too big")))))))

(defn print-actor!
  [actor]
  (debug "Actor:" (:type actor))
  (debug (pprint-str actor)))

(defn install!
  [game]
  (debug "Installing debug module")
  (-> game
      (game/install-event-effect #'print-event!)
      (game/install-event-effect [:ui.event/actor-clicked :right] (comp #'print-actor! :actor))))