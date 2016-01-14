(ns falx.thing
  "Things are just maps, each game entity is a thing"
  (:require [falx.react :as react]
            [falx.point :as point]
            [falx.location :as location])
  (:import (java.util UUID)))

(defonce ^:private reactions (atom {}))

(defn thing
  ([id]
   (thing id {}))
  ([id template]
   (merge template {:id id} )))

(defn fresh-thing
  ([]
   (fresh-thing {}))
  ([template]
   (thing (str (UUID/randomUUID)) template)))

(defn fresh-thing-seq
  ([template]
   (repeatedly #(fresh-thing template)))
  ([template n]
   (repeatedly n #(fresh-thing template))))

(defn defreaction
  [event-type key f]
  (swap! reactions react/register event-type key f))

(defn react
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

(defn put-at-point
  "Puts the thing at the `point`, assuming its staying on the same level"
  [thing point]
  (if-some [level (:level thing)]
    (put thing (location/cell level point))
    thing))

(defn adjacent-to-point?
  "Is the thing adjacent to the point?"
  [thing point]
  (and point
       (when-some [current (:point thing)]
         (point/adjacent? current point))))

(defn adjacent-to-cell?
  "Is the thing adjacent to the cell?"
  [thing cell]
  (when (:cell thing)
    (location/adjacent? (:cell thing) cell)))

(defn step
  "Puts the thing in the `point` only if it is adjacent to it."
  [thing point]
  (if (adjacent-to-point? thing point)
    (put-at-point thing point)
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