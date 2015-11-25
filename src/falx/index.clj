(ns falx.index
  (:require [clojure.set :as set]))

(defn disjoc
  [m k v]
  (let [s (get m k)]
    (if-some [new (not-empty (disj s v))]
      (assoc m k new)
      (dissoc m k))))

(defn disjoc-in
  [m [k & ks] v]
  (if (seq ks)
    (if-some [m2 (not-empty (disjoc-in (get m k) ks v))]
      (assoc m k m2)
      (dissoc m k))
    (disjoc m k v)))

(defn dissoc-in
  [m [k & ks]]
  (if (seq ks)
    (if-some [m2 (not-empty (dissoc-in (get m k) ks))]
      (assoc m k m2)
      (dissoc m k))
    (dissoc m k)))

(defn find
  [m id]
  (-> m :eav (get id)))

(defn find-att
  ([m id k]
   (get (find m id) k))
  ([m id k not-found]
   (get (find m id) k not-found)))

(defn find-ids-with
  [m k v]
  (-> m :ave (get k) (get v) (or #{})))

(defn find-with
  [m k v]
  (map #(find m %) (find-ids-with m k v)))

(defn dissoc-att
  ([m id k]
   (let [now (find-att m id k)]
     (-> (disjoc-in m [:ave k now] id)
         (dissoc-in [:eav id k]))))
  ([m id k & ks]
   (reduce #(dissoc-att %1 id %2) (dissoc-att m id k) ks)))

(defn assoc-att
  ([m id k v]
   (-> (dissoc-att m id k)
       (assoc-in [:eav id k] v)
       (update-in [:ave k v] (fnil conj #{}) id))))

(defn merge-atts
  [m id m2]
  (reduce-kv #(assoc-att %1 id %2 %3) m m2))

(defn add
  ([m]
   (let [id (:id m 0)]
     (-> (assoc m :id (inc id))
         (assoc-att id :id id))))
  ([m initial]
   (let [id (:id m 0)]
     (-> (add m)
         (merge-atts id initial)))))

(defn delete
  [m id]
  (reduce #(dissoc-att %1 id %2) m (keys (find m id))))

(defn reingest
  [m id m2]
  (let [e (find m id)
        old (set e)
        new (set m2)
        add (set/difference new old)
        del (set/difference old new)
        without (reduce #(dissoc-att %1 id (key %2)) m del)]
    (reduce #(assoc-att %1 id (key %2) (val %2)) without add)))

(defn ingest
  [m entity]
  (if-some [id (:id entity)]
    (-> (delete m id)
        (reingest id entity))
    (add m entity)))

(defn ask
  ([m id f]
   (when-some [e (find m id)]
     (f e)))
  ([m id f & args]
   (ask m id #(apply f % args))))

(defn change
  ([m id f]
   (if-some [e (find m id)]
     (reingest m id (f e))
     m))
  ([m id f & args]
   (change m id #(apply f % args))))