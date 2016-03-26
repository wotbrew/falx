(ns falx.io.print
  (:require [clojure.pprint :as pp]
            [clojure.tools.logging :refer [debug]]))

(defn pprint-str
  [o]
  (with-out-str
    (pp/pprint o)))

(defn- assoc-some
  [m k v]
  (if (contains? m k)
    (assoc m k v)
    m))

(def silent-message?
  #{:game.event/frame
    :request/tick-ai
    :request/print-message
    :ai.event/tick-complete})

(def display-message-details?
  #{[:creature.event/goal-given :goal.type/move]
    :creature.event/unselected
    :creature.event/selected})

(defn message!
  [msg]
  (let [type (:type msg)]
    (when (and (not (:silent? msg))
               (not (silent-message? type)))
      (debug "Message:" type)
      (when (display-message-details? type)
        (debug (pprint-str (assoc-some msg :world "too big")))))))

(defn actor!
  [actor]
  (debug "Actor:" (:type actor))
  (debug (pprint-str actor)))