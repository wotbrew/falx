(ns falx.io.debug
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
  #{:event/frame
    :event/multi
    :event/actor-goal-added
    :event/actor-goal-removed
    :event/actor-changed
    [:event/actor-changed :actor.type/creature]
    :event/ai-tick-complete
    :request/give-goal
    :request/tick-ai
    :request/in-ms
    :request/print-message})

(def display-message-details?
  #{})

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