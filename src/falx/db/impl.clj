(ns falx.db.impl
  (:refer-clojure :exclude [assert]))

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

(defn index-eav
  [m id k v]
  (assoc-in m [id k] v))

(defn index-ave
  [m id k v ev]
  (cond->
    m
    (not (identical? ev ::not-found)) (disjoc-in [k ev] id)
    :always (update-in [k v] (fnil conj #{}) id)))

(defn assert
  [db id k v]
  (let [{eav :falx.db/eav
         ave :falx.db/ave} db
        ev (-> eav (get id) (get k ::not-found))]
    (if (= ev v)
      db
      (assoc db :falx.db/eav (index-eav eav id k v)
                :falx.db/ave (index-ave ave id k v ev)))))

(defn retract
  ([db id k]
   (retract db id k (-> db :falx.db/eav (get id) (get k))))
  ([db id k v]
   (-> db
       (disjoc-in [:falx.db/ave k v] id)
       (dissoc-in [:falx.db/eav id k]))))
