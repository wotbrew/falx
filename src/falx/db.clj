(ns falx.db
  "Contains a low-level value indexed eav database."
  (:require [falx.db.impl :as impl]
            [clojure.set :as set])
  (:refer-clojure :exclude [replace assert alter]))

(defn exists?
  "Returns true if the entity exists."
  [db id]
  (-> db ::eav (contains? id)))

(defn assert
  "Adds (or sets) the value of `k` for the entity to `v`."
  ([db id k v]
   (if-not (exists? db id)
     (-> (impl/assert db id ::id id)
         (recur id k v))
     (impl/assert db id k v)))
  ([db id k v & kvs]
   (let [ret (assert db id k v)]
     (if kvs
       (if (next kvs)
         (recur ret id (first kvs) (second kvs) (nnext kvs))
         (throw (IllegalArgumentException.
                  "assert expects even number of arguments after db, found odd number")))
       ret))))

(defn entity
  "Returns a map describing the state of the entity."
  [db id]
  (-> db ::eav (get id)))

(defn ids
  "Returns a seq of ids"
  [db]
  (-> db ::eav keys))

(defn retract
  "Removes the value against `k` from the db for `id`.
  If removing the last attribute of an entity, will not remove the entity.
  Does not allow you to remove the :falx.db/id attribute."
  ([db id k]
   (if (identical? k ::id)
     (throw (IllegalArgumentException. "Cannot retract :falx.db/id (delete instead)"))
     (impl/retract db id k (get (entity db id) k))))
  ([db id k & ks]
   (reduce #(retract db id %) (retract db id k) ks)))

(defn delete
  "Completely removes the entity from the db."
  ([db id]
   (let [ks (keys (entity db id))]
     (reduce #(impl/retract %1 id %2) db ks)))
  ([db id & more]
   (reduce delete (delete db id) more)))

(defn add
  "Adds or merges the m to the db using the key :falx.db/id to determine entity identity."
  ([db m]
   (let [id (::id m ::not-found)]
     (if (identical? id ::not-found)
       (throw (IllegalArgumentException. "map `m` needs to contain :falx.db/id key"))
       (reduce-kv #(assert %1 id %2 %3) db m))))
  ([db m & more]
   (reduce add (add db m) more)))

(defn replace
  "Replaces all attributes of the entity with those in `m`. Requires the :falx.db/id key to be present."
  ([db m]
   (let [id (::id m ::not-found)]
     (if (identical? id ::not-found)
       (throw (IllegalArgumentException. "map `m` needs to contain :falx.db/id key"))
       (as-> db db
             (delete db id)
             (add db m)))))
  ([db m & more]
   (reduce replace (replace db m) more)))

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
  ([db k v]
   (-> db ::ave (get k) (get v) (or #{})))
  ([db k v & kvs]
   (let [x (iquery db k v)]
     (if (seq kvs)
       (if (nnext kvs)
         (set/intersection
           x
           (iquery db (first kvs) (second kvs) (nnext kvs)))
         (throw (IllegalArgumentException. "iquery expects even number of arguments after db, found odd number")))
       x))))

(defn query
  "Queries for entities where `k` == `v`."
  ([db k v]
   (map #(entity db %) (iquery db k v)))
  ([db k v & kvs]
   (let [x (query db k v)]
     (if (seq kvs)
       (if (nnext kvs)
         (set/intersection
           x
           (query db (first kvs) (second kvs) (nnext kvs)))
         (throw (IllegalArgumentException. "query expects even number of arguments after db, found odd number")))
       x))))

(defn alter
  ([db id f]
    (if-some [e (entity db id)]
      (replace db (f e))
      (add db (f {::id id}))))
  ([db id f & args]
    (alter db id #(apply f % args))))