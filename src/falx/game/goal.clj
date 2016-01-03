(ns falx.game.goal
  (:require [falx.thing :as thing]))

(defn give
  "Gives the goal to the thing"
  [thing goal]
  (-> (update thing :goals conj goal)
      (thing/publish-event {:type  [:event.thing/goal-added (:type goal)]
                            :thing thing
                            :goal  goal})))
(defn give-coll
  "Gives the coll of goals to the thing"
  [thing coll]
  (reduce give thing coll))

(defn- remove*
  [thing goal]
  (update thing :goals (partial remove #{goal})))

(defn complete
  "Completes the given goal, returns the thing."
  [thing goal]
  (-> (remove* thing goal)
      (thing/publish-event {:type  [:event.thing/goal-completed (:type goal)]
                            :thing thing
                            :goal  goal})))
(defn complete-coll
  "Comples a coll of goals, returns the thing."
  [thing coll]
  (reduce complete thing coll))

(defn discard
  "Discards the goal, returns the thing."
  [thing goal]
  (-> (remove* thing goal)
      (thing/publish-event {:type  [:event.thing/goal-discarded (:type goal)]
                            :thing thing
                            :goal  goal})))

(defn discard-coll
  "Discards a coll of goals, returns the thing."
  [thing coll]
  (reduce discard thing coll))

(defn same-type?
  "Do the goals share the same type?"
  [goal-a goal-b]
  (= (:type goal-a)
     (:type goal-b)))

(defn discard-same-type
  "Discard goals from the the thing that share the same type as the given goal."
  [thing goal]
  (->> (filter #(same-type? goal %) (:goals thing))
       (discard-coll thing)))

(defn give-exclusive
  "Gives the goal to the thing, discarding any goals of the same type."
  [thing goal]
  (-> (discard-same-type thing goal)
      (give goal)))

(defn move-to-point
  "Returns a move to point goal."
  [point]
  {:type :goal/move-to-point
   :point point})