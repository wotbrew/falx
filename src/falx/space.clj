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

(defn position
  [map point layer]
  {:map map
   :point point
   :layer layer})

(defn space
  [layer-sort-map]
  {:sort-order layer-sort-map})

(defn position->cell
  [position]
  (cell (:map position) (:point position)))

(defn cell->position
  [cell layer]
  (position (:map cell) (:point cell) layer))

(defn find-position
  [space id]
  (-> space :id (get id)))

(defn find-cell
  [space id]
  (when-some [pos (find-position space id)]
    (position->cell pos)))

(defn find-in-position
  [space position]
  (-> space :position (get position)))

(defn find-in-cell
  [space cell]
  (-> space :cell (get cell)))

(defn find-in-map
  [space map]
  (-> space :map (get map)))

(defn unput
  [space id]
  (if-some [pos (find-position space id)]
    (let [cell (position->cell pos)]
      (-> space
          (dissoc-in [:id id])
          (disjoc-in [:position position] id)
          (disjoc-in [:cell cell] id)
          (disjoc-in [:map (:map cell)] id)))
    space))

(defn put
  [space id position]
  (let [cell (position->cell position)]
    (-> (unput space id)
        (assoc-in [:id id] position)
        (update-in [:position position] set-conj id)
        (update-in [:cell cell] set-conj id)
        (update-in [:map (:map cell)] set-conj id))))