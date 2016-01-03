(ns falx.world
  (:require [clojure.set :as set]
            [falx.thing :as thing]
            [falx.util :as util]))

(defn publish-event
  ([world event]
   (update world :events (fnil conj []) event)))

(defn split-events
  [world]
  {:world (dissoc world :events)
   :events (:events world)})

(defn get-thing
  [world id]
  (-> world :eav (get id)))

(defn get-attribute
  ([world id attribute]
   (get-attribute world id attribute nil))
  ([world id attribute not-found]
   (-> (get-thing world id) (get attribute not-found))))

(defn get-ids-by-value
  [world attribute value]
  (-> world :ave (get attribute) (get value #{})))

(defn get-things-by-value
  [world attribute value]
  (map #(get-thing world %) (get-ids-by-value world attribute value)))

(defn remove-attribute*
  [world id attribute]
  (let [value (get-attribute world id attribute ::not-found)]
    (case value
      ::not-found world
      (-> world
          (util/dissoc-in [:eav id attribute])
          (util/disjoc-in [:ave attribute value] id)))))

(defn remove-attribute
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
  [world id]
  (let [thing (get-thing world id)
        attributes (keys thing)]
    (-> (reduce #(remove-attribute* %1 id %2) world attributes)
        (publish-event
          {:type :event.thing/removed
           :id id}))))

(defn add-thing*
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
  ([world id f]
   (let [thing (get-thing world id)]
     (add-thing world (f thing))))
  ([world id f & args]
   (update-thing world id #(apply f % args))))