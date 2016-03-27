(ns falx.world
  "A datastructure representing the world, or space in which the game takes place.
  The world contains actors, each identified by an `:id` that denote the entities in the game."
  (:require [falx.db :as db]
            [falx.actor :as actor]
            [falx.react :as react]
            [falx.event :as event])
  (:refer-clojure :exclude [empty]))

(def empty
  "The empty world."
  {:db db/empty
   :reactions {}
   :events []})

(defn world
  "Creates a new world, can provide a coll of reactions."
  ([]
   (world []))
  ([reactions]
   (assoc empty :reactions (react/react-map reactions))))

(defn publish
  "Propogates a new event into to the world."
  [world event]
  (let [world' (react/react world (:reactions world) event)]
    (update world' :events conj event)))

(defn split-events
  "Removes any pending events from the world, returns a map
  of `:world`, and `:events`."
  [world]
  (let [w (assoc world :events [])]
    {:world  w
     :events (:events world)}))

(defn get-actor
  "Returns the actor given by `id` or `nil` if none is found."
  [world id]
  (-> world :db (db/pull id)))

(defn query-actors
  "Finds all actors in the world with the attribute `k` having the value `v`."
  ([world k v]
   (-> world :db (db/pull-query k v)))
  ([world k v & kvs]
   (let [db (:db world)]
     (apply db/pull-query db k v kvs))))

(defn get-all-actors
  "Returns a seq of all actors in the world (in no particular order)."
  [world]
  (db/get-entities (:db world)))

(defn- split-actor-events
  [world actor]
  (let [{:keys [actor events]} (actor/split-events actor)
        ea (get-actor world (:id actor))]
    {:actor actor
     :events
            (cond
              (nil? ea)
              [(event/actor-created actor)
               (event/actor-changed
                 nil
                 actor)
               (event/multi events)]

              (not= ea actor)
              [(event/actor-changed ea actor)
               (event/multi events)]

              :else events)}))

(defn replace-actor
  "Replaces the actor in the world with the one provided. Assumes the actor contains `:id`."
  [world actor]
  (let [{:keys [actor events]} (split-actor-events world actor)]
    (as-> world world
          (update world :db db/replace actor)
          (reduce publish world events))))

(defn add-actor
  "Same as `replace-actor`"
  [world actor]
  (replace-actor world actor))

(defn update-actor
  "Applies the function `f` and any args to the actor given by `id`.
  Replacing it with the resultant value. Returns the new world."
  ([world id f]
   (if-some [actor (get-actor world id)]
     (replace-actor world (f actor))
     world))
  ([world id f & args]
   (update-actor world id #(apply f % args))))

(defn remove-actor
  "Removes the actor from the world."
  [world id]
  (update world :db db/remove id))

(defn get-at
  "Returns a coll of actors at the given cell"
  [world cell]
  (query-actors world :cell cell))

(defn get-obstructions-at
  "Returns all actors obstructing the `actor` at the given `cell`."
  [world actor cell]
  (actor/get-obstructions actor (get-at world cell)))

(defn some-obstruction-at
  "Returns the first actor obstructing the `actor` at the given `cell`."
  [world actor cell]
  (actor/some-obstruction actor (get-at world cell)))

(defn some-obstructs?
  "True if an actor is obstructing the `actor` at the given `cell`."
  [world actor cell]
  (some? (some-obstruction-at world actor cell)))

(defn can-put?
  "Is it possible to place the `actor` at the given `cell`?"
  [world actor cell]
  (not (some-obstructs? world actor cell)))

(defn solid-at?
  "Is there a solid entity at the given cell?"
  [world cell]
  (some :solid? (get-at world cell)))

(defn put
  "If possible, places the actor given by `id` at the given cell. Returns the new world."
  [world id cell]
  (let [actor (get-actor world id)]
    (if (and actor (can-put? world actor cell))
      (update-actor world id actor/put cell)
      world)))

(defn unput
  "Removes the location info from the actor given by `id`."
  [world id]
  (update-actor world id actor/unput))

(defn can-step?
  "Is it possible for the `actor` to step to the `cell`?"
  [world actor cell]
  (and (can-put? world actor cell)
       (actor/can-step? actor cell)))

(defn step
  "If possible puts the actor in the `cell` by stepping.
  The actor must be adjacent to the cell.
  Returns the new world."
  [world id cell]
  (let [actor (get-actor world id)]
    (if (and actor (can-step? world actor cell))
      (update-actor world id actor/put cell)
      world)))