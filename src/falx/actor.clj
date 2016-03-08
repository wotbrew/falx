(ns falx.actor
  "Contains functions on actors within the game. Each actor is a map
  identified by its :id."
  (:require [falx.react :as react]
            [falx.position :as pos])
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
     :events (mapv #(assoc % :actor a) (:events actor))}))

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

(defn put-event
  [old-cell cell]
  {:type :actor.event/put
   :old-cell old-cell
   :cell cell})

(defn unput-event
  [cell]
  {:type :actor.event/unput
   :cell cell})

(defn put
  "Puts the actor at the `cell`, removes it from its old position.
  Returns the actor."
  [actor cell]
  (let [a (change-position actor cell)
        old-cell (:cell actor)
        moved? (not= old-cell (:cell a))]
    (cond->
      a
      moved? (publish (put-event old-cell cell))
      old-cell (publish (unput-event cell)))))

(defn unput
  "Removes location information from the `actor`. Returns the actor."
  [actor]
  (if (some? (:cell actor))
    (-> (dissoc actor :cell :point :level :slice)
        (publish (unput-event (:cell actor))))
    actor))

(defn can-step?
  "Is the actor able to step into the `cell`?"
  [actor cell]
  (adjacent-to-cell? actor cell))

(defn step
  "Attempts to step into the `cell`. Returns the `actor`, possibly at the new `cell`."
  [actor cell]
  (if (can-step? actor cell)
    (put actor cell)
    actor))