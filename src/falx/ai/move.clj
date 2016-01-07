(ns falx.ai.move
  (:require [falx.event :as event]
            [clojure.core.async :as async]
            [falx.state :as state]
            [falx.game.goal :as goal]
            [falx.thing :as thing]
            [falx.point :as point]))

;; ========
;; TAKE A STEP

(defn step-goal
  "Return a step goal"
  [point]
  {:type :goal/step
   :point point})

(defn try-step
  [thing point goal]
  (let [thing' (thing/step thing point)]
    (if (= point (:point thing'))
      (goal/complete thing' goal)
      (goal/discard thing' goal))))

(event/defhandler
  [:event.thing/goal-added :goal/step]
  ::step-goal-added
  (fn [event]
    (let [{:keys [goal thing]} event
          point (:point goal)
          id (:id thing)]
      (state/update-thing! id try-step point goal))))

;; ============
;; ASKED TO MOVE

(defn find-path-goal
  "Returns a path goal"
  [point]
  {:type :goal/find-path
   :point point})

(defn try-move-to-cell
  [thing cell goal]
  (if (thing/adjacent-to-cell? thing cell)
    (goal/give-exclusive thing (step-goal (:point goal)))
    (goal/give-exclusive thing (find-path-goal (:point cell)))))

(event/defhandler
  [:event.thing/goal-added :goal/move-to-cell]
  ::move-goal-added
  (fn [event]
    (let [{:keys [goal thing]} event
          cell (:cell goal)
          id (:id thing)]
      (state/update-thing! id try-move-to-cell cell goal))))

;; ==============
;; FINDING PATH

(defn walk-path
  "Returns a walk path goal"
  [path]
  {:type :goal/walk-path
   :path path})

(defn get-path
  [point-a point-b]
  (seq (rest (point/get-a*-path (constantly true) point-a point-b))))

(defn try-walk-path
  [thing path goal]
  (if (seq path)
    (-> (goal/give-exclusive thing (walk-path path))
        (goal/complete goal))
    (goal/fail thing goal)))

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
        (let [path (get-path current point)]
          (state/update-thing! id try-walk-path path goal))))))

;; ==========
;; WALKING PATH

(def walk-wait-time 125)

(defn try-path-step
  [thing point goal]
  (if-not (goal/has? thing goal)
    thing
    (goal/give-exclusive thing (step-goal point))))

(event/defhandler
  [:event.thing/goal-added :goal/walk-path]
  ::walk-path-added
  (fn [event]
    (let [{:keys [goal thing]} event
          id (:id thing)
          path (:path goal)]
      (async/go-loop
        [path path]
        (if (and (seq path) (goal/has? (state/get-thing id) goal))
          (let [point (first path)]
            (state/update-thing! id try-path-step point goal)
            (async/<! (async/timeout walk-wait-time))
            (if (= (:point (state/get-thing id)) point)
              (recur (rest path))
              (state/update-thing! id goal/fail goal)))
          (state/update-thing! id goal/complete goal))))))