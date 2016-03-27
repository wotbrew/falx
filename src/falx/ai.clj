(ns falx.ai
  (:require [falx.point :as point]
            [falx.actor :as actor]
            [falx.request :as request]
            [falx.goal :as goal]
            [falx.position :as pos]))

(defmulti act (fn [world actor goal] (:type goal)))

(defmethod act :default
  [world actor goal])

(defmethod act :goal.type/move
  [world actor {:keys [cell]}]
  (when-not (or (actor/has-goal? actor :goal.type/find-path)
                (actor/has-goal? actor :goal.type/walk-path))
    [(request/give-goal
       actor
       (goal/find-path cell))
     (request/tick-ai actor 100)]))

(defmethod act :goal.type/find-path
  [world actor {:keys [cell]}]
  (when-not (actor/has-goal? actor :goal.type/walk-path)
    (let [to (:point cell)
          level (:level cell)
          from (:point actor)
          path (when from (point/get-a*-path (constantly true) from to))]
      (when (seq path)
        [(request/give-goal
           actor
           (goal/walk-path
             (mapv #(pos/cell % level) path)))
         (request/tick-ai actor 100)]))))

(defn tick
  [world actor]
  (mapcat #(act world actor %) (actor/get-goals actor)))