(ns falx.db
  "An implementation of an entity database, useful for indexing arbitrary attributes
  and querying them."
  (:require [clojure.set :as set]))

(defn- disjoc
  [m k v]
  (let [s (get m k)]
    (if-some [new (not-empty (disj s v))]
      (assoc m k new)
      (dissoc m k))))

(defn- disjoc-in
  [m [k & ks] v]
  (if (seq ks)
    (if-some [m2 (not-empty (disjoc-in (get m k) ks v))]
      (assoc m k m2)
      (dissoc m k))
    (disjoc m k v)))

(defn- dissoc-in
  [m [k & ks]]
  (if (seq ks)
    (if-some [m2 (not-empty (dissoc-in (get m k) ks))]
      (assoc m k m2)
      (dissoc m k))
    (dissoc m k)))

(def ^:private set-conj
  (fnil conj #{}))

(defn get-all
  "Returns a seq of all the entities in the db."
  [db]
  (-> db :eav vals))

(defn get-entity
  "Returns the entity given by the `id`."
  [db id]
  (-> db :eav (get id)))

(defn exists?
  "Returns true if the entity exists."
  [db id]
  (contains? (:eav db) id))

(defn iquery
  "Returns all the entity ids having the attribute `k` with the value `v`.
  If multiple pairs (or a map) is supplied, intersects all entity ids that meet each kv pair."
  ([db m]
   (reduce-kv #(set/intersection %1 (iquery db %2 %3)) #{} m))
  ([db k v]
   (-> db :ave (get k) (get v #{})))
  ([db k v & kvs]
   (iquery db (into {k v} (partition 2 kvs)))))

(defn query
  "Returns all entities having the attribute `k` with the value `v`.
  If multiple pairs (or a map) is supplied, intersects all entity ids that meet each kv pair."
  ([db m]
   (map #(get-entity db %) (iquery db m)))
  ([db k v]
   (map #(get-entity db %) (iquery db k v)))
  ([db k v & kvs]
   (query db (into {k v} (partition 2 kvs)))))

(defn add-entity
  "Adds or replaces an entity in the database.
  Returns the new db."
  ([db entity]
   (let [id (:id entity)
         existing (get-entity db id)
         ave (as-> (:ave db) ave
                   (reduce-kv (fn [ave k v]
                                (if (= v (get entity k ::not-found))
                                  ave
                                  (disjoc-in ave [k v] id)))
                              ave existing)
                   (reduce-kv (fn [ave k v]
                                (if (= v (get existing k ::not-found))
                                  ave
                                  (update-in ave [k v] set-conj id)))
                              ave entity))]
     (-> (assoc-in db [:eav id] entity)
         (assoc :ave ave))))
  ([db entity & more]
   (reduce add-entity (add-entity db entity) more)))

(defn remove-entity
  "Removes the entity from the db. Returns the new db."
  ([db id]
   (if-some [existing (get-entity db id)]
     (let [ave (reduce-kv (fn [ave k v]
                            (disjoc-in ave [k v] id))
                          (:ave db)
                          existing)]
       (-> (dissoc-in db [:eav id])
           (assoc :ave ave)))))
  ([db id & more]
   (reduce remove-entity (remove-entity db id) more)))

(defn update-entity
  "Apples `f` and any `args` to the entity given by `id`.
  Returns the new db."
  ([db id f]
   (if-some [existing (get-entity db id)]
     (add-entity db (f existing))
     db))
  ([db id f & args]
   (update-entity db id #(apply f % args))))

(defn db
  "Creates a new db, optionally initializing with the given entity collection `ecoll`."
  ([]
   {:eav {}
    :ave {}})
  ([ecoll]
   (reduce add-entity (db) ecoll)))