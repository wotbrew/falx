(ns falx.db
  "Contains a low-level value indexed eav database."
  (:require [falx.db.impl :as impl])
  (:refer-clojure :exclude [replace assert]))

(defn exists?
  "Returns true if the entity exists."
  [db id]
  (-> db ::eav (contains? id)))

(defn assert
  "Adds (or sets) the value of `k` for the entity to `v`."
  [db id k v]
  (if-not (exists? db id)
    (-> (impl/assert db id ::id id)
        (recur id k v))
    (impl/assert db id k v)))

(defn entity
  "Returns a map describing the state of the entity."
  [db id]
  (-> db ::eav (get id)))

(defn- retract*
  [db id k v]
  (-> db
      (impl/disjoc-in [::ave k v] id)
      (impl/dissoc-in [::eav id k])))

(defn retract
  "Removes the value against `k` from the db for `id`.
  If removing the last attribute of an entity, will not remove the entity.
  Does not allow you to remove the :falx.db/id attribute."
  [db id k]
  (if (identical? k ::id)
    db
    (retract* db id k (get (entity db id) k))))

(defn delete
  "Completely removes the entity from the db."
  [db id]
  (let [ks (keys (entity db id))]
    (reduce delete db ks)))

(defn add
  "Adds or merges the m to the db using the key :falx.db/id to determine entity identity."
  [db m]
  (let [id (::id m ::not-found)]
    (if (identical? id ::not-found)
      db
      (reduce-kv #(assert %1 id %2 %3) db m))))

(defn replace
  "Replaces all attributes of the entity with those in `m`. Requires the :falx.db/id key to be present."
  [db m]
  (as-> db db
        (delete db (::id m))
        (add db m)))

(defn db
  "Creates a new db"
  ([]
   (db []))
  ([ecoll]
   (reduce add {} ecoll)))

(defn entity?
  "Returns whether `x` is an entity."
  [x]
  (and (map? x)
       (some? (::id x))))

(defn iquery
  "Queries for entity ids where `k` == `v`."
  [db k v]
  (-> db ::ave (get k) (get v) (or #{})))

(defn query
  "Queries for entities where `k` == `v`."
  [db k v]
  (map #(entity db %) (iquery db k v)))