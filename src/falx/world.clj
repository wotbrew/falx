(ns falx.world
  (:require [falx.util :refer :all])
  (:refer-clojure :exclude [empty]))

(def empty {:seed 0
            :eav {}
            :ave {}})

(defn get-entity
  [world eid]
  (-> world :eav (get eid)))

(defn get-eids-with
  [world attribute value]
  (-> world :ave (get attribute) (get value)))

(defn get-entities-with
  [world attribute value]
  (map #(get-entity world %) (get-eids-with world attribute value)))

(defn add-eid-attribute
  [world eid attribute value]
  (-> world
      (assoc-in [:eav eid attribute] value)
      (update-in [:ave attribute value] set-conj eid)))

(defn get-eid-attribute
  ([world eid attribute]
   (-> world :eav (get eid) (get attribute)))
  ([world eid attribute not-found]
   (-> world :eav (get eid) (get attribute not-found))))

(defn remove-eid-attribute
  [world eid attribute]
  (let [val (get-eid-attribute world eid attribute ::nope)]
    (if (identical? ::nope val)
      world
      (-> world
          (dissoc-in [:eav eid attribute])
          (disjoc-in [:ave attribute val] eid)))))

(defn remove-eid
  [world eid]
  (reduce #(remove-eid-attribute %1 eid %2) world (keys (get-entity world eid))))

(defn add-entity
  [world entity]
  (let [id (:id entity (:seed world 0))
        entity' (assoc entity :id id)
        world' (remove-eid world id)
        world'' (update world' :seed (fnil inc 0))]
    (reduce-kv #(add-eid-attribute %1 id %2 %3) world'' entity')))

(defn add-entities
  [world entities]
  (reduce add-entity world entities))

(defn update-entity
  ([world eid f]
    (if-some [entity (get-entity world eid)]
      (add-entity world (f entity))
      world))
  ([world eid f & args]
    (update-entity world eid #(apply f % args))))

(defmulti act
  (fn [world action]
    (:type action)))

(defmethod act :default
  [world action]
  world)

(defmethod act :add-entity
  [world {:keys [entity]}]
  (add-entity world entity))