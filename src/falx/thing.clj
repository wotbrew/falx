(ns falx.thing
  "Things are just maps, each game entity is a thing"
  (:require [falx.react :as react]))

(defonce ^:private reactions (atom {}))

(defn defreaction!
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

(defn cell
  "The cell represents a point in the world, each point
  can be located at a `level`."
  [level point]
  {:level level
   :point point})

(defn slice
  "The slice represents a layer in the world, each layer
  can be located at a `level`."
  [level layer]
  {:level level
   :layer layer})

(defn put
  "Puts the thing in the given `cell` in the world."
  [thing cell]
  (if (= cell (:cell thing))
    thing
    (-> (assoc thing
          :cell cell
          :slice (slice (:level cell) (:layer thing :unknown))
          :point (:point cell)
          :level (:level cell))
        (publish-event
          {:type  :event.thing/put
           :thing thing
           :cell  cell}))))

(defn step
  "Puts the thing in the given `point`, assuming its staying on the same level"
  [thing point]
  (if-some [level (:level thing)]
    (put thing (cell level point))
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

