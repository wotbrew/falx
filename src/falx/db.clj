(ns falx.db
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

(defn get-entity
  "Returns the entity given by the `id`."
  [g id]
  (-> g :eav (get id)))

(defn exists?
  "Returns true if the entity exists."
  [g id]
  (contains? (:eav g) id))

(defn get-attr
  "Returns the attribute `k` of the entity given by `id`."
  ([g id k]
   (-> g :eav (get id) (get k)))
  ([g id k not-found]
   (-> g :eav (get id) (get k not-found))))

(defn has-attr?
  "Returns whether the entity has the attribute `k`."
  [g id k]
  (-> g :eav (get id) (contains? k)))

(defn ihaving
  "Returns all entity ids having the attribute(s) `k` & `ks`."
  ([g k]
   (-> g :ae (get k)))
  ([g k & ks]
   (reduce #(set/intersection %1 (ihaving g %2)) #{} (cons k ks))))

(defn having
  "Returns the entitys having the attribute(s) `k` & `ks`."
  ([g k]
   (map #(get-entity g %) (ihaving g k)))
  ([g k & ks]
   (reduce #(set/intersection %1 (having g %2)) #{} (cons k ks))))

(defn iquery
  "Returns all the entity ids having the attribute `k` with the value `v`.
  If multiple pairs (or a map) is supplied, intersects all entity ids that meet each kv pair."
  ([g m]
   (reduce-kv #(set/intersection %1 (iquery g %2 %3)) #{} m))
  ([g k v]
   (-> g :ave (get k) (get v #{})))
  ([g k v & kvs]
   (iquery g (into {k v} (partition 2 kvs)))))

(defn query
  "Returns all entities having the attribute `k` with the value `v`.
  If multiple pairs (or a map) is supplied, intersects all entity ids that meet each kv pair."
  ([g m]
   (map #(get-entity g %) (iquery g m)))
  ([g k v]
   (map #(get-entity g %) (iquery g k v)))
  ([g k v & kvs]
   (query g (into {k v} (partition 2 kvs)))))

(defn rem-attr
  "Removes the attribute `k` from the entity given by `id`."
  ([g id k]
   (let [v (get-attr g id k ::not-found)]
     (if (identical? ::not-found v)
       g
       (-> g
           (dissoc-in [:eav id k])
           (disjoc-in [:ae k] id)
           (disjoc-in [:ave k v] id)))))
  ([g id k & ks]
   (reduce #(rem-attr %1 id %2) g (cons k ks))))

(defn set-attr
  "Sets the attribute `k` to `v` on the entity given by `id`."
  ([g id k v]
   (if (= v (get-attr g id k ::not-found))
     g
     (-> g
         (rem-attr id k)
         (assoc-in [:eav id k] v)
         (update-in [:ae k] set-conj id)
         (update-in [:ave k v] set-conj id))))
  ([g id k v & kvs]
   (->> (cons [k v] (partition 2 kvs))
        (reduce #(set-attr %1 id (first %2) (second %2)) g))))

(defn update-attr
  "Applies the function `f` and any `args` to the attribute `k` on the entity given by `id`."
  ([g id k f]
   (let [v (get-attr g id k)]
     (set-attr g id k (f v))))
  ([g id k f & args]
   (update-attr g id k #(apply f % args))))

(defn rem-entity
  "Removes the entity given by `id` from the game."
  [g id]
  (let [ks (keys (get-entity g id))]
    (reduce #(rem-attr %1 id %2) g ks)))

(defn merge-entity
  ([g a]
   (merge-entity g (:id a) a))
  ([g id m]
   (reduce-kv #(set-attr %1 id %2 %3) g m))
  ([g id m & more]
   (reduce #(merge-entity %1 id %2) g (cons m more))))

(defn add-entity
  ([g a]
   (add-entity g (or (:id a) (inc (:max-id g -1))) a))
  ([g id m]
   (let [g (if (number? id)
             (update g :max-id (fnil max 0) id)
             g)
         ea (get-entity g id)]
     (-> (reduce-kv #(if (contains? m %2)
                      %1
                      (rem-attr %1 id %2 %3)) g ea)
         (merge-entity id m)))))

(defn add-entity-coll
  ([g ecoll]
   (reduce add-entity g ecoll))
  ([g ecoll & more]
   (reduce add-entity-coll g (cons ecoll more))))

(defn update-entity
  ([g id f]
   (if-some [a (get-entity g id)]
     (add-entity g (f a))
     g))
  ([g id f & args]
   (update-entity g id #(apply f % args))))

(defn db
  ([]
   {})
  ([ecoll]
   (add-entity-coll (db) ecoll)))