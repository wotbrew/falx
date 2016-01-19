(ns falx.ai.move
  (:require [falx.event :as event]
            [clojure.core.async :as async]
            [falx.state :as state]
            [falx.game.goal :as goal]
            [falx.thing :as thing]
            [falx.game.path :as path]
            [falx.world :as world]
            [falx.game.move :as move]))

(def walk-wait-time 125)

(event/defhandler-async
  [:event.thing/goal-changed :goal/goto-cell]
  ::goto-cell
  (fn [{:keys [thing goal]}]
    (let [id (:id thing)
          cell (:cell goal)]
      (async/go-loop
        [path nil]
        (let [world (state/get-world)
              thing (world/get-thing world id)
              current-cell (:cell thing)
              next-cell (first path)]
          (cond
            (not (goal/has? thing goal)) nil
            (= current-cell cell) (state/update-thing-async! id goal/complete goal)
            (nil? next-cell) (recur (path/get-path world current-cell cell))
            (= current-cell next-cell) (recur (rest path))
            :else (do
                    (state/update-world-async! move/step-for-goal id next-cell goal)
                    (async/<! (async/timeout walk-wait-time))
                    (recur path))))))))


(event/defhandler-async
  [:event.thing/goal-changed :goal/goto-thing]
  ::goto-thing
  (fn [{:keys [thing goal]}]
    (let [target-thing (:thing goal)
          target-id (:id target-thing)
          id (:id thing)]
      (async/go-loop
        [path nil
         target-cell nil]
        (let [world (state/get-world)
              thing (world/get-thing world id)
              target-thing (world/get-thing world target-id)
              current-cell (:cell thing)
              next-cell (first path)]
          (cond
            (not (goal/has? thing goal)) nil
            (thing/adjacent-to-cell? target-thing current-cell) (state/update-thing-async! id goal/complete goal)
            (= current-cell target-cell) (recur nil nil)
            (nil? target-cell) (recur nil (move/get-nearest-cell world thing target-thing))
            (nil? next-cell) (recur (path/get-path world current-cell target-cell) target-cell)
            (= current-cell next-cell) (recur (rest path) target-cell)
            :else (do
                    (state/update-world-async! move/step-for-goal id next-cell goal)
                    (async/<! (async/timeout walk-wait-time))
                    (recur path target-cell))))))))