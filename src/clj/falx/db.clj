(ns falx.db
  (:refer-clojure :exclude [empty assert])
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

(defn- retract
  [db id k]
  (-> db
      (update :eav util/dissoc-in [id k])
      (update :ave util/disjoc-in [k (get (entity db id) k)] id)))

(defn- assert
  [db id k v]
  (let [db (retract db id k)]
    (-> db
        (assoc-in [:eav id k] v)
        (assoc-in [:eav id :db/id] id)
        (update-in [:ave k v] (fnil conj #{}) id))))

(defn tempid
  ([]
   (UUID/randomUUID)))

(defmulti eval-txfn (fn [db tx] (nth tx 0)))

(defn transact
  [db tx-data]
  (let [id-seed (volatile! (:id-seed db 0))
        tempids (volatile! {})
        resolve-id (fn [id]
                     (cond
                       (nil? id)
                       (recur (tempid))
                       (and (number? id) (neg? id))
                       (or (get @tempids id)
                           (let [rid (vswap! id-seed inc)]
                             (vswap! tempids assoc id rid)
                             rid))
                       :else id))]
    {:tempids   @tempids
     :db-before db
     :db-after (->
                 (reduce
                   (fn ! [db tx]
                     (if (map? tx)
                       (let [id (resolve-id (:db/id tx))
                             tx (assoc tx :db/id id)]
                         (reduce-kv #(assert %1 id %2 %3) db tx))
                       (case (nth tx 0)
                         :db/add (assert db (resolve-id (nth tx 1)) (nth tx 2) (nth tx 3))
                         :db/retract (retract db (resolve-id (nth tx 1)) (nth tx 2))
                         :db/update (let [id (resolve-id (nth tx 1))
                                          k (nth tx 2)
                                          f (nth tx 3)
                                          args (subvec tx 4)
                                          ev (get (entity db id) k)]
                                      (assert db id k (apply f ev args)))
                         :db/update-entity (let [id (resolve-id (nth tx 1))
                                                 f (nth tx 2)
                                                 args (subvec tx 3)
                                                 e (assoc (entity db id) :db/id id)]
                                             (! db (apply f e args)))
                         :db/retract-entity (let [id (resolve-id (nth tx 1))
                                                  m (entity db id)]
                                              (reduce-kv (fn [db k _] (retract db id k)) db m))
                         (reduce ! db (eval-txfn db tx)))))
                   db
                   tx-data)
                 (assoc :id-seed @id-seed))}))

(defn resolve-tempid
  [tx-result id]
  (-> tx-result :tempids (get id)))