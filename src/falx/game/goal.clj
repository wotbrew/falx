(ns falx.game.goal
  (:require [falx.thing :as thing]))

(defn then
  "Composes 2 goals so that one goal-b is performed if goal-a is successful"
  ([goal-a goal-b]
   (assoc goal-a :then goal-b)))

(defn has?
  "Does the thing have the goal?"
  [thing goal]
  (= (:goal thing) goal))

(defn has-exact?
  [thing goal goal-id]
  (and (has? thing goal)
       (= (:goal-id thing) goal-id)))

(defn give
  "Gives the goal to the thing"
  [thing goal]
  (-> (assoc thing :goal goal)
      (update :goal-id (fnil inc 0))
      (thing/publish-event {:type  [:event.thing/goal-changed (:type goal)]
                            :thing thing
                            :goal  goal
                            :goal-id (inc (:goal-id thing 0))})))

(defn- remove*
  [thing goal]
  (if (has? thing goal)
    (dissoc thing :goal)
    thing))

(defn complete
  "Completes the given goal, returns the thing."
  [thing goal]
  (if (has? thing goal)
    (cond-> (remove* thing goal)
            :then (thing/publish-event {:type  [:event.thing/goal-completed (:type goal)]
                                        :thing thing
                                        :goal  goal})
            (:then goal) (give (:then goal)))
    thing))

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