(ns falx.ai
  (:require [falx.point :as point]
            [falx.actor :as actor]
            [falx.request :as request]
            [falx.goal :as goal]
            [falx.position :as pos]
            [falx.world :as world]))

(defmulti react (fn [world actor event] (:type event)))

(defmulti goal-react (fn [world actor goal event] [(:type goal) (:type event)]))

(defmethod react :default
  [_ _ _])

(defmethod goal-react :default
  [_ _ _ _])

(defmulti goal-added (fn [world actor goal] (:type goal)))

(defmethod goal-added :default
  [_ _ _])

(defmulti goal-removed (fn [world actor goal] (:type goal)))

(defmethod goal-removed :default
  [_ _ _])

(defmethod react :event/actor-goal-added
  [world actor event]
  (goal-added world actor (:goal event)))

(defmethod react :event/actor-goal-removed
  [world actor event]
  (goal-removed world actor (:goal event)))

(defmethod goal-added :goal.type/move
  [world actor {:keys [cell]}]
  [(request/give-goal actor (goal/find-path cell))])

(defmethod goal-added :goal.type/find-path
  [world actor {:keys [cell]}]
  (let [origin-cell (:cell actor)
        level (:level origin-cell)
        point-path (when origin-cell
                     (point/get-a*-path #(not (world/solid-at? world (pos/cell % level)))
                                        (:point origin-cell)
                                        (:point cell)))
        path (rest (map #(pos/cell % level) point-path))]
    (when (seq path)
      [(request/give-goal actor (goal/walk-path path))])))

(defmethod goal-added :goal.type/walk-path
  [world actor {:keys [path]}]
  (when (seq path)
    [(request/give-goal actor (goal/step (first path)))]))

(defmethod goal-react [:goal.type/walk-path :event/actor-stepped]
  [world actor {:keys [path]} {:keys [cell]}]
  (when (= (first path) cell)
    [(-> (request/give-goal actor (goal/continue
                                    (first (actor/get-goals actor :goal.type/move))
                                    (goal/walk-path (rest path))))
         (request/in-ms 100))]))

(defmethod goal-added :goal.type/step
  [world actor {:keys [cell]}]
  (when (actor/can-step? actor cell)
    [(request/step actor cell)]))

(defmethod goal-react [:goal.type/step :event/actor-stepped]
  [world actor goal event]
  (when (= (:cell goal) (:cell event))
    [(request/remove-goal actor goal)]))

(defmethod goal-added :goal.type/continue
  [world actor {:keys [pred goal]}]
  (when (actor/has-goal? actor pred)
    [(request/give-goal actor goal)]))

(defn tick
  [world actor event]
  (concat
    (react world actor event)
    (mapcat #(goal-react world actor % event) (actor/get-goals actor))))