(ns falx.world
  "Core functions on the world.
  The world is simply an indexed collection of things."
  (:require [clojure.set :as set]
            [falx.thing :as thing]
            [falx.util :as util]))

(defn publish-event
  "Publishes an event that can be reacted to.
  Returns a new world which will retain the `event` under its
  `:events` key."
  ([world event]
   (update world :events (fnil conj []) event)))

(defn split-events
  "Removes events from the world, returns
  a map {:world, :events} where :events will contain
  the events that had been published so far."
  [world]
  {:world (dissoc world :events)
   :events (:events world)})

(defn get-things
  "Returns a seq of all things in the world."
  [world]
  (-> world :eav vals))

(defn get-thing
  "Returns the thing with `id`"
  [world id]
  (-> world :eav (get id)))

(defn get-attribute
  "Gets a single thing attribute"
  ([world id attribute]
   (get-attribute world id attribute nil))
  ([world id attribute not-found]
   (-> (get-thing world id) (get attribute not-found))))

(defn get-ids-by-value
  "Returns the ids of things having
  the attribute, value pair."
  [world attribute value]
  (-> world :ave (get attribute) (get value #{})))

(defn get-things-by-value
  "Returns the things having the given attribute
  value pair."
  [world attribute value]
  (map #(get-thing world %) (get-ids-by-value world attribute value)))

(defn- remove-attribute*
  [world id attribute]
  (let [value (get-attribute world id attribute ::not-found)]
    (case value
      ::not-found world
      (-> world
          (util/dissoc-in [:eav id attribute])
          (util/disjoc-in [:ave attribute value] id)))))

(defn remove-attribute
  "Removes an attribute from a thing."
  [world id attribute]
  (let [value (get-attribute world id attribute ::not-found)]
    (case value
      ::not-found world
      (-> (remove-attribute* world id attribute)
          (publish-event
            {:type :event.thing/attribute-removed
             :id id
             :attribute attribute
             :value value})))))

(defn set-attribute
  "Sets the things attribute to the given valie."
  [world id attribute value]
  (-> (remove-attribute* world id attribute)
      (assoc-in [:eav id attribute] value)
      (update-in [:ave attribute value] util/set-conj id)
      (cond->
        (not= value (get-attribute world id attribute))
        (publish-event
          {:type           :event.thing/attribute-set
           :id             id
           :attribute      attribute
           :value          value
           :previous-value (get-attribute world id attribute)}))))

(defn remove-thing
  "Completely removes a thing from the world."
  [world id]
  (let [thing (get-thing world id)
        attributes (keys thing)]
    (-> (reduce #(remove-attribute* %1 id %2) world attributes)
        (publish-event
          {:type :event.thing/removed
           :id id}))))

(defn- add-thing*
  [world thing]
  {:pre [(some? (:id thing))]}
  (let [id (:id thing)
        existing-thing (get-thing world (:id thing))
        ekvs (set existing-thing)
        {:keys [thing events]} (thing/split-events thing)
        kvs (set thing)]
    (as->
      world g
      (reduce #(set-attribute %1 id (key %2) (val %2)) g (set/difference kvs ekvs))
      (reduce #(remove-attribute %1 id (key %2)) g (set/difference ekvs kvs))
      (reduce publish-event g events))))

(defn add-thing
  "Adds the thing to the world, if it already exists attributes
  are removed or added as appropriate.

  Each thing must be provided with an `:id`."
  [world thing]
  (let [existing (get-thing world (:id thing))]
    (cond
      (nil? existing)
      (-> (add-thing* world thing)
          (publish-event
            {:type :event.thing/added
             :id   thing}))
      (not= existing thing)
      (-> (add-thing* world thing)
          (publish-event
            {:type :event.thing/changed
             :id   thing}))
      :else world)))

(defn update-thing
  "Applies the function `f` and any `args` to the thing
  in the world given by `id`."
  ([world id f]
   (let [thing (get-thing world id)]
     (add-thing world (f thing))))
  ([world id f & args]
   (update-thing world id #(apply f % args))))