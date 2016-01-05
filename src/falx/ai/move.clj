(ns falx.ai.move
  (:require [falx.event :as event]
            [clojure.core.async :as async]))

(event/defhandler
  [:event.thing/goal-added :goal/move-to-cell]
  ::move-goal-added
  (fn [event]
    (let [{:keys [goal thing]} event
          cell (:cell goal)]
      (async/go
        (prn "will move" (:id thing) cell)))))