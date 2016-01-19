(ns falx.game.goal
  (:require [falx.thing :as thing]))

(defn has?
  "Does the thing have the goal?"
  [thing goal]
  (= (:goal thing) goal))

(defn give
  "Gives the goal to the thing"
  [thing goal]
  (-> (assoc thing :goal goal)
      (thing/publish-event {:type  [:event.thing/goal-changed (:type goal)]
                            :thing thing
                            :goal  goal})))

(defn- remove*
  [thing goal]
  (if (has? thing goal)
    (dissoc thing :goal)
    thing))

(defn complete
  "Completes the given goal, returns the thing."
  [thing goal]
  (-> (remove* thing goal)
      (thing/publish-event {:type  [:event.thing/goal-completed (:type goal)]
                            :thing thing
                            :goal  goal})))

(defn discard
  "Discards the goal, returns the thing."
  [thing goal]
  (-> (remove* thing goal)
      (thing/publish-event {:type  [:event.thing/goal-discarded (:type goal)]
                            :thing thing
                            :goal  goal})))

(defn fail
  "Removes the goal due to failure, returns the thing"
  [thing goal]
  (-> (remove* thing goal)
      (thing/publish-event {:type [:event.thing/goal-failed (:type goal)]
                            :thing thing
                            :goal goal})))