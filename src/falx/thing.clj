(ns falx.thing
  "Things are just maps, each game entity is a thing"
  (:require [falx.react :as react]
            [falx.point :as point]
            [falx.location :as location])
  (:import (java.util UUID)))

(defonce ^:private reactions (atom {}))

(defn thing
  "Creates a thing from the template using the given id."
  ([id]
   (thing id {}))
  ([id template]
   (merge template {:id id} )))

(defn fresh-thing
  "Creates a thing from the template, assigns a unique id."
  ([]
   (fresh-thing {}))
  ([template]
   (thing (str (UUID/randomUUID)) template)))

(defn fresh-thing-seq
  "Generates a seq of unique things from the given template."
  ([template]
   (repeatedly #(fresh-thing template)))
  ([template n]
   (repeatedly n #(fresh-thing template))))

(defn defreaction
  "Defines a reaction to an event published to/by the thing.
  Each reaction is a function of the thing plus the event, and is executed
  after immediately after the event is published. The reaction should return the new entity."
  [event-type key f]
  (swap! reactions react/register event-type key f))

(defn- react
  [thing event]
  (react/react @reactions event thing))

(defn split-events
  "A thing that is being operated on may accumulate events, these can be split out from the thing
  into a map of {:thing, :events}"
  [thing]
  {:thing (dissoc thing :events)
   :events (:events thing)})

(defn publish-event
  "**Publishes** the fact something has changed with the thing, all this does is conj it into the things
  `:events` collection. Returns the new thing."
  [thing event]
  (-> (update thing :events (fnil conj []) event)
      (react event)))

(defn thing?
  "Is the map a thing?"
  [m]
  (some? (:id m)))

(defn in?
  "Is the thing in the cell?"
  [thing cell]
  (= (:cell thing) cell))

(defn put
  "Puts the thing in the given `cell` in the world."
  [thing cell]
  (if (= cell (:cell thing))
    thing
    (-> (assoc thing
          :cell cell
          :slice (location/slice (:level cell) (:layer thing :unknown))
          :point (:point cell)
          :level (:level cell))
        (publish-event
          {:type  :event.thing/put
           :thing thing
           :cell  cell}))))

(defn adjacent-to-cell?
  "Is the thing adjacent to the cell?"
  [thing cell]
  (when (:cell thing)
    (location/adjacent? (:cell thing) cell)))

(defn adjacent?
  "Are the 2 things adjacent to one another?"
  [thing-a thing-b]
  (let [cell-a (:cell thing-a)
        cell-b (:cell thing-b)]
    (when (and cell-a cell-b)
      (location/adjacent? cell-a cell-b))))

(defn get-nearest-cell
  "Gets the nearest cell to the thing in `cells`"
  [thing cells]
  (when-some [cell (:cell thing)]
    (location/get-nearest cell cells)))

(defn in-level?
  "Is the thing in the given level?"
  [thing level]
  (= (:level thing) level))

(defn step
  "Puts the thing at the `cell` only if it is adjacent to it"
  [thing cell]
  (if (adjacent-to-cell? thing cell)
    (put thing cell)
    thing))

(defn unput
  "Removes the thing from its position in the world."
  [thing]
  (-> (dissoc thing :cell :slice :point :level)
      (publish-event
        {:type :event.thing/unput
         :thing thing})))

(defn same-thing?
  "Are thing-a and thing-b potentially different snapshots of the same thing?
  i.e do they have equal `:id` identity?"
  [thing-a thing-b]
  (= (:id thing-a)
     (:id thing-b)))

(defn coll-remove
  "Removes other instances of the thing from the coll"
  [thing coll]
  (remove #(same-thing? thing %) coll))

(defn coll-difference
  "Takes those things in things-b that are not in things-a. Comparison is done by thing identity."
  [things-a things-b]
  (reduce #(coll-remove %2 %1) things-b things-a))