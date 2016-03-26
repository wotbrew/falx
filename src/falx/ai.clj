(ns falx.ai
  (:require [falx.creature :as creature]))

(defn tick-complete-event
  ([actor]
   (tick-complete-event actor 100))
  ([actor timeout]
   {:type    :ai.event/tick-complete
    :timeout timeout
    :actor   actor}))

(defmulti act (fn [world creature goal] (:type goal)))

(defmethod act :default
  [world creature goal])

(defmethod act :goal.type/move
  [world creature {:keys [cell]}]
  (when-not (-> creature :goals (contains? :goal.type/find-path))
    #_[{:type  :request/find-path
      :actor creature
      :cell  cell}]))

(defmethod act :goal.type/find-path
  [world creature {:keys [cell]}]
  )

(defn tick
  [world creature]
  (concat
    (mapcat #(act world creature %) (creature/get-goals creature))
    [(tick-complete-event creature)]))