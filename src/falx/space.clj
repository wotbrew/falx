(ns falx.space)

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

(defn cell
  [map point]
  {:map map
   :point point})

(def default
  {})

(defn find-cell
  [space id]
  (-> space :id (get id)))

(defn list-in-cell
  [space cell]
  (-> space :cell (get cell)))

(defn list-in-map
  [space map]
  (-> space :map (get map)))

(defn unput
  [space id]
  (if-some [cell (find-cell space id)]
    (-> space
        (dissoc-in [:id id])
        (disjoc-in [:cell cell] id)
        (disjoc-in [:map (:map cell)] id))
    space))

(defn put
  [space id cell]
  (-> (unput space id)
      (assoc-in [:id id] cell)
      (update-in [:cell cell] set-conj id)
      (update-in [:map (:map cell)] set-conj id)))