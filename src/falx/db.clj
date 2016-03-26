(ns falx.db
  "Defines a set of functions on a simple in memory entity-attribute-value database."
  (:require [falx.util :as util]
            [clojure.set :as set])
  (:refer-clojure :exclude [update merge replace assert empty remove]))

(def empty
  "An empty database"
  {:eav {}
   :ave {}})

(defn query
  "Returns the set of entity ids with the attribute `k` having the value `v`.
  For multiple kvs, returns entities having each `k` and `v`."
  ([db k v]
   (-> db :ave (get k) (get v) (or #{})))
  ([db k v & kvs]
   (loop [r (query db k v)
          kvs kvs]
     (if (empty? r)
       r
       (if (seq kvs)
         (let [[k v & tail] kvs]
           (recur (set/intersection r (query db k v))
                  tail))
         r)))))

(defn pull
  "Returns the entity given by its `id`."
  [db id]
  (-> db :eav (get id)))

(defn pull-query
  "Returns the set of entities with the attribute `k` having the value `v`.
  For multiple kvs, returns entities having each `k` and `v`."
  ([db k v]
   (map (partial pull db) (query db k v)))
  ([db k v & kvs]
   (map (partial pull db) (apply query db k v kvs))))

(defn get-entities
  "Returns a seq of all entities in the db (in no particular order)."
  [db]
  (vals (:eav db)))

(defn- assert-eav
  [eav id k v]
  (assoc-in eav [id k] v))

(defn- assert-ave
  [ave id k v]
  (update-in ave [k v] util/set-conj id))

(defn- retract-eav
  [eav id k]
  (util/dissoc-in eav [id k]))

(defn- retract-ave
  [ave id k v]
  (util/disjoc-in ave [k v] id))

(defn assert
  "Introduces the fact that `k` is `v` for the entity given by `id`. Returns the new db."
  [db id k v]
  (let [{:keys [eav ave]} db
        e (get eav id)
        ev (get e k ::not-found)]
    (if (= ev v)
      db
      (assoc db :eav (assert-eav eav id k v)
                :ave (cond->
                       (assert-ave ave id k v)
                       (not= ev ::not-found) (retract-ave id k ev))))))

(defn retract
  "Removes the attribute `k` from the entity given by `id`. Returns the new db."
  [db id k]
  (let [e (pull db id)
        ev (get e k ::not-found)]
    (if (= ev ::not-found)
      db
      (-> (clojure.core/update db :eav retract-eav id k)
          (clojure.core/update :ave retract-ave id k ev)))))

(defn replace
  "For a map `m` with an `:id` representing entity identity. Use the keys of the map as entity attributes in the db.
  Returns the new db. Removes any attributes already on the entity that are not present in `m`."
  [db m]
  (let [id (:id m)
        e (pull db id)
        eks (set (keys e))
        nks (set (keys m))
        retracts (set/difference eks nks)]
    (as-> db db
          (reduce #(retract %1 id %2) db retracts)
          (reduce-kv #(assert %1 id %2 %3) db m))))

(defn add
  "The same as `replace`"
  [db m]
  (replace db m))

(defn remove
  "Removes the entity given by its `id`. Returns the new db"
  [db id]
  (let [ks (keys (pull db id))]
    (reduce #(retract %1 id %2) db ks)))

(defn update
  "Applies the function `f` and any `args` to the entity given by its `id`. Returns the new db."
  ([db id f]
   (if-some [e (pull db id)]
     (replace db (assoc (f e) :id id))
     db))
  ([db id f & args]
   (update db id #(apply f % args))))