(ns falx.ai.move
  (:require [falx.event :as event]
            [clojure.core.async :as async]
            [falx.state :as state]
            [falx.game.goal :as goal]
            [falx.game.move :as move]
            [falx.thing :as thing]
            [falx.game.path :as path]))


;; ============
;; ASKED TO MOVE

(defn find-path-goal
  "Returns a path goal"
  [cell]
  {:type :goal/find-path
   :cell cell})

(defn try-move-to-cell
  [thing cell]
  (if (thing/adjacent-to-cell? thing cell)
    (move/step thing cell)
    (goal/give-exclusive thing (find-path-goal cell))))

(event/defhandler
  [:event.thing/goal-added :goal/move]
  ::move-goal-added
  (fn [event]
    (let [{:keys [goal thing]} event
          cell (:cell goal)
          id (:id thing)]
      (state/update-thing! id try-move-to-cell cell))))

;; ==============
;; FINDING PATH

(defn walk-path
  "Returns a walk path goal"
  [path]
  {:type :goal/walk-path
   :path path})

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
          current (:cell thing)
          cell (:cell goal)
          id (:id thing)
          world (:world (state/get-game))]
      (if-not current
        (state/update-thing! id goal/discard goal)
        (let [path (path/get-path world current cell)]
          (state/update-thing! id try-walk-path path goal))))))

;; ==========
;; WALKING PATH

(def walk-wait-time 125)

(defn try-path-step
  [thing cell goal]
  (if-not (goal/has? thing goal)
    thing
    (move/step thing cell)))

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
          (let [cell (first path)]
            (state/update-thing! id try-path-step cell goal)
            (async/<! (async/timeout walk-wait-time))
            (if (= (:cell (state/get-thing id)) cell)
              (recur (rest path))
              (state/update-thing! id goal/fail goal)))
          (state/update-thing! id goal/complete goal))))))