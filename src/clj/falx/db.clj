(ns falx.db
  (:refer-clojure :exclude [empty assert alter])
  (:require [clojure.set :as set]
            [falx.util :as util])
  (:import (java.util UUID)))

(defrecord DB [eav ave id-seed])

(def empty
  (->DB {} {} 0))

(defn entity
  [db id]
  (-> db :eav (get id)))

(defn query
  ([db k v]
   (-> db :ave (get k) (get v) (or #{})))
  ([db k v & kvs]
   (loop [ret (query db k v)
          kvs kvs]
     (if-some [[k v & kvs] (seq kvs)]
       (recur (set/intersection ret (query db k v)) kvs)
       ret))))

(defn equery
  ([db k v]
   (map (:eav db) (query db k v)))
  ([db k v & kvs]
   (map (:eav db) (apply query db k v kvs))))

(defn with
  ([db k]
   (query db k true))
  ([db k & ks]
   (apply query db k (mapcat vector ks (repeat true)))))

(defn ewith
  ([db k]
   (equery db k true))
  ([db k & ks]
   (apply equery db k (mapcat vector ks (repeat true)))))

(defn retract
  [db id k]
  (-> db
      (update :eav util/dissoc-in [id k])
      (update :ave util/disjoc-in [k (get (entity db id) k)] id)))

(defn assert
  [db id k v]
  (let [db (retract db id k)]
    (-> db
        (assoc-in [:eav id k] v)
        (assoc-in [:eav id :db/id] id)
        (update-in [:ave k v] (fnil conj #{}) id))))

(defn alter
  ([db id k f]
   (let [ev (get (entity db id) k)]
     (assert db id k (f ev))))
  ([db id k f & args]
    (alter db id k #(apply f % args))))

(defn next-id
  [db]
  [(inc (:id-seed db 0))
   (update db :id-seed (fnil inc 0))])

(defn add
  [db m]
  (if-some [id (:db/id m)]
    (reduce-kv #(assert %1 id %2 %3) db m)
    (let [[id db] (next-id db)]
      (recur db (assoc m :db/id id)))))

(defn alter-entity
  ([db id f]
    (add db (f (assoc (entity db id) :db/id id))))
  ([db id f & args]
    (alter-entity db id #(apply f % args))))

(defn retract-entity
  ([db id]
   (reduce-kv (fn [db k _] (retract db id k)) db (entity db id))))

(defn tempid
  ([]
   (UUID/randomUUID)))

(defn transact
  [db tx-data]
  (reduce
    (fn ! [db tx]
      (if (map? tx)
        (add db tx)
        (let [f (first tx)
              args (rest tx)]
          (apply f db args))))
    db
    tx-data))