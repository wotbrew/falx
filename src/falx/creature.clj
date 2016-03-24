(ns falx.creature
  (:require [falx.actor :as actor]))

(defn creature?
  [actor]
  (= (:type actor) :actor.type/creature))

(defn selectable?
  [creature]
  (creature? creature))

(defn can-select?
  [creature]
  (and (selectable? creature)
       (not (:selected? creature))))

(defn select
  [creature]
  (if (can-select? creature)
    (-> (assoc creature :selected? true)
        (actor/publish {:type :creature.event/selected}))
    creature))

(defn can-unselect?
  [creature]
  (and (selectable? creature)
       (:selected? creature)))

(defn unselect
  [creature]
  (if (can-unselect? creature)
    (-> (dissoc creature :selected?)
        (actor/publish {:type :creature.event/unselected}))
    creature))

(defn toggle-select
  [creature]
  (if (:selected? creature)
    (unselect creature)
    (select creature)))

(defn goal-removed-event
  [goal]
  {:type     [:creature.event/goal-removed (:type goal)]
   :goal goal})

(defn remove-goal
  [creature goal]
  (-> (update-in creature [:goals (:type goal)] disj goal)
      (actor/publish (goal-removed-event goal))))

(defn get-goals
  [creature type]
  (-> creature :goals (get type)))

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

(defn move
  [creature cell]
  (give-goal creature (move-goal cell)))