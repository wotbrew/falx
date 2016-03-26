(ns falx.creature
  (:require [falx.actor :as actor]))

(defn creature?
  [actor]
  (= (:type actor) :actor.type/creature))

(defn goal-removed-event
  [goal]
  {:type     [:creature.event/goal-removed (:type goal)]
   :goal goal})

(defn remove-goal
  [creature goal]
  (-> (update-in creature [:goals (:type goal)] disj goal)
      (actor/publish (goal-removed-event goal))))

(defn get-goals
  ([creature]
   (into [] cat (-> creature :goals vals)))
  ([creature type]
   (-> creature :goals (get type))))

(defn goal-added-event
  [goal]
  {:type     [:creature.event/goal-given (:type goal)]
   :goal goal})

(defn give-goal
  [creature goal]
  (-> (if (:exclusive? goal)
        (reduce remove-goal creature (get-goals creature (:type goal)))
        creature)
      (update-in [:goals (:type goal)] (fnil conj #{}) goal)
      (actor/publish
        (goal-added-event goal))))

(defn has-goal?
  [creature goal]
  (-> creature :goals (get (:type goal)) (contains? goal)))

(defn move-goal
  [cell]
  {:type :goal.type/move
   :exclusive? true
   :cell cell})

(defn find-path-goal
  [cell]
  {:type :goal.type/find-path
   :exclusive? true
   :cell cell})