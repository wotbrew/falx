(ns falx.actor
  "Contains functions on actors within the game. Each actor is a map
  identified by its :id."
  (:require [falx.react :as react]
            [falx.position :as pos]
            [falx.event :as event]
            [falx.util :as util])
  (:refer-clojure :exclude [empty]))

(defn actor
  "Creates a new actor, with the attributes defined in the map `m`."
  ([id]
   (actor id {}))
  ([id m]
   (merge {:id        id
           :events    []
           :reactions {}}
          m)))

(defn split-events
  "Returns a map of
    :events - the list of pending events on the actor
    :actor - the actor without its events"
  [actor]
  (let [a (assoc actor :events [])]
    {:actor  a
     :events (:events actor)}))

(defn publish
  "Publishes an event to the actor"
  [actor event]
  (let [actor' (react/react actor (:reactions actor) event)]
    (update actor' :events conj event)))

(defn at-cell?
  "Is the actor at the given cell?"
  [actor cell]
  (= (:cell actor) cell))

(defn at-point?
  "Is the actor at the given point?"
  [actor point]
  (= (:point actor) point))

(defn at-level?
  "Is the actor at/on the given level?"
  [actor level]
  (= (:level actor) level))

(defn obstructs?
  "Is the actor `a1` obstructed by the actor `a2`?"
  [a1 a2]
  ;;execute this way round for common case speed
  (and (:solid? a2) (:solid? a1)))

(defn get-obstructions
  "Returns all actors in `acoll` that would obstruct the `actor`."
  [actor acoll]
  (filter #(obstructs? actor %) acoll))

(defn some-obstruction
  "Returns the first actor in `acoll` that would obstruct the `actor`."
  [actor acoll]
  (first (get-obstructions actor acoll)))

(defn some-obstructs?
  "Does any actor in `acoll` obstruct the `actor`?"
  [actor acoll]
  (some? (some-obstruction actor acoll)))

(defn adjacent-to-cell?
  "Is the `actor` adjacent to the given `cell`?"
  [actor cell]
  (when-some [c (:cell actor)]
    (pos/adjacent? c cell)))

(defn adjacent-to-actor?
  "Is the actor `a1` adjacent to the actor `a2`?"
  [a1 a2]
  (when-some [c (:cell a2)]
    (pos/adjacent? a1 c)))

(defn- change-position
  [actor cell]
  (if (at-cell? actor cell)
    actor
    (assoc actor
      :cell cell
      :point (:point cell)
      :level (:level cell)
      :slice (pos/slice (:layer actor) (:level cell)))))

(defn put
  "Puts the actor at the `cell`, removes it from its old position.
  Returns the actor."
  [actor cell]
  (let [a (change-position actor cell)
        old-cell (:cell actor)
        moved? (not= old-cell (:cell a))]
    (cond->
      a
      moved? (publish (event/actor-put a old-cell cell))
      old-cell (publish (event/actor-unput a cell)))))

(defn unput
  "Removes location information from the `actor`. Returns the actor."
  [actor]
  (if (some? (:cell actor))
    (as-> actor x
          (dissoc x :cell :point :level :slice)
          (publish x (event/actor-unput x (:cell actor))))
    actor))

(defn can-step?
  "Is the actor able to step into the `cell`?"
  [actor cell]
  (adjacent-to-cell? actor cell))

(defn step
  "Attempts to step into the `cell`. Returns the `actor`, possibly at the new `cell`."
  [actor cell]
  (if (can-step? actor cell)
    (as-> actor x
          (put x cell)
          (publish x (event/actor-stepped x cell)))
    actor))

(defn creature?
  [actor]
  (= (:type actor) :actor.type/creature))

(defn get-goals
  ([actor]
   (into [] cat (-> actor :goals vals)))
  ([actor goal]
   (-> actor :goals (get (:type goal goal)))))

(defn remove-goal
  [actor goal]
  (if (keyword? goal)
    (reduce remove-goal actor (get-goals actor goal))
    (as-> actor x
          (util/disjoc-in x [:goals (:type goal)] goal)
          (publish x (event/actor-goal-removed x goal)))))

(defn give-goal
  [actor goal]
  (as-> actor x
        (reduce remove-goal x (:removes goal))
        (update-in x [:goals (:type goal)] (fnil conj #{}) goal)
        (publish x (event/actor-goal-added x goal))))

(defn has-goal?
  [actor goal]
  (if (keyword? goal)
    (-> actor :goals (get goal) seq some?)
    (-> actor :goals (get (:type goal)) (contains? goal))))