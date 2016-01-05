(ns falx.ai.move
  (:require [falx.event :as event]
            [clojure.core.async :as async]
            [falx.state :as state]
            [falx.game.goal :as goal]
            [falx.thing :as thing]
            [falx.point :as point]))

(defn find-path-goal
  "Returns a path goal"
  [point]
  {:type :goal/find-path
   :point point})

(defn walk-path
  "Returns a walk path goal"
  [points]
  {:type :goal/walk-path
   :points points})

(defn step-goal
  "Return a step goal"
  [point]
  {:type :goal/step
   :point point})

(event/defhandler
  [:event.thing/goal-added :goal/move-to-cell]
  ::move-goal-added
  (fn [event]
    (let [{:keys [goal thing]} event
          cell (:cell goal)
          id (:id thing)]
      (state/update-thing! id goal/give-exclusive (find-path-goal (:point cell))))))

(defn get-path
  [point-a point-b]
  (seq (rest (point/get-a*-path (constantly true) point-a point-b))))

(event/defhandler-async
  [:event.thing/goal-added :goal/find-path]
  ::find-path-goal-added
  (fn [event]
    (let [{:keys [goal thing]} event
          current (:point thing)
          point (:point goal)
          id (:id thing)]
      (if-not current
        (state/update-thing! id goal/discard goal)
        (let [points (get-path current point)]
          (state/update-thing!
            id
            (fn [thing]
              (if (seq points)
                (-> (goal/give-exclusive thing (walk-path points))
                    (goal/complete goal))
                (goal/fail thing goal)))))))))

(event/defhandler
  [:event.thing/goal-added :goal/step]
  ::step-goal-added
  (fn [event]
    (let [{:keys [goal thing]} event
          point (:point goal)
          id (:id thing)]
      (let [thing (state/update-thing! id thing/step point)]
        (if (= (:point thing) point)
          (state/update-thing! id goal/complete goal)
          (state/update-thing! id goal/discard goal))))))

(event/defhandler
  [:event.thing/goal-added :goal/walk-path]
  ::walk-path-added
  (fn [event]
    (let [{:keys [goal thing]} event
          id (:id thing)
          points (:points goal)]
      (async/go-loop
        [points points]
        (if (and (seq points) (goal/has? (state/get-thing id) goal))
          (do (state/update-thing!
                id
                (fn [thing]
                  (if-not (goal/has? thing goal)
                    thing
                    (goal/give-exclusive thing (step-goal (first points))))))
              (async/<! (async/timeout 250))
              (recur (rest points)))
          (state/update-thing! id goal/complete goal))))))