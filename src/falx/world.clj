(ns falx.world
  "Functions on the world, maintaining a model of spatial and physical properties of things in the world.
  A world is made up of things paired to particular identities.

  An identity can map to any thing, but maps are given special treatment.
  A map is placable in space by its `:cell`. It can have the properties `:solid?` and `:opaque?`.
  An other abitrary keys are accepted, but the above are specially indexed for efficient query."
  (:require [falx.space :as space]
            [falx.point :as point]))

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

(defn get-thing
  "Returns the value of the thing given by `id`."
  [w id]
  (-> w :things (get id)))

;; ===
;; Solidness

(defn- solid-at?*
  [w cell]
  ;;manually calculated
  (let [{:keys [things space]
         :or {things {}}} w]
    (some (comp :solid? things) (space/get-at space cell))))

(defn solid-at?
  "Is the given cell solid?"
  ([w cell]
   (solid-at? w (:level cell) (:point cell)))
  ([w level point]
    ;;uses the fast index
   (-> w :solid-index (get level) (get point)))
  ([w level x y]
   (solid-at? w level [x y])))

;; ===
;; Light

(defn- opaque-at?*
  [w cell]
  ;;manually calculated
  (let [{:keys [things space]
         :or {things {}}} w]
    (some (comp :opaque? things) (space/get-at space cell))))

(defn opaque-at?
  "Is the given cell opaque to light?"
  ([w cell]
   (opaque-at? w (:level cell) (:point cell)))
  ([w level point]
    ;;uses the fast index
   (-> w :opaque-index (get level) (get point)))
  ([w level x y]
   (opaque-at? w level [x y])))

;; ===
;; Validation

(defn valid-cell?
  "Is the given cell valid? i.e could the value be placed there?"
  ([w v cell]
   (valid-cell? w ::not-found v cell))
  ([w id v cell]
   (or (space/at? (:space w) id cell)
       (not (and (:solid? v)
                 (solid-at? w cell))))))

(defn valid-flood
  "Returns an infinite seq of valid cells via flood-fill from the given cell."
  ([w v cell]
   (valid-flood w ::not-found v cell))
  ([w id v cell]
   (filter #(valid-cell? w id v %) (space/flood cell))))

(defn get-nearest-valid-cell
  "Returns the nearest valid cell to `cell`. May just return `cell` if it is itself valid."
  ([w v cell]
   (get-nearest-valid-cell w ::not-found v cell))
  ([w id v cell]
   (if (valid-cell? w id v cell)
     cell
     (first (valid-flood w id v cell)))))

(defn validate-cell
  "Makes sure the values cell is actually valid. Returns a new value with a valid cell."
  ([w v]
   (validate-cell w ::not-found v))
  ([w id v]
   (let [cell (:cell v)]
     (if (or (not cell)
             (valid-cell? w id v cell))
       v
       (assoc v :cell (get-nearest-valid-cell w v cell))))))

;; ===
;; Pathing

(defn path
  "Finds a path from the thing to the cell."
  [w id cell]
  (when-some [v (get-thing w id)]
    (when-some [ocell (:cell v)]
      (let [level (:level cell)
            walkable? (if (:solid? v)
                        #(not (solid-at? w level %))
                        (constantly true))]
        (point/get-a*-path walkable? (:point ocell) (:point cell))))))

;; ===
;; Changing things

(def ^:private default-layer
  nil)

(defn- sync-space
  [w id v]
  (if (:cell v)
    (update w :space space/move id (:layer v default-layer) (:cell v))
    w))

(defn- sync-solid-index
  [w v]
  (if-some [cell (:cell v)]
    (if (solid-at?* w cell)
      (assoc-in w [:solid-index (:level cell) (:point cell)] true)
      (dissoc-in w [:solid-index (:level cell) (:point cell)]))
    w))

(defn- sync-opaque-index
  [w v]
  (if-some [cell (:cell v)]
    (if (opaque-at?* w cell)
      (assoc-in w [:opaque-index (:level cell) (:point cell)] true)
      (dissoc-in w [:opaque-index (:level cell) (:point cell)]))
    w))

(defn- sync-indexes
  ([w v]
   (-> w
       (sync-solid-index v)
       (sync-opaque-index v)))
  ([w v oldv]
    ;; because the indexes are positional, we must sync
    ;; both the previous cell and the new one.
   (-> w
       (sync-indexes oldv)
       (sync-indexes v))))

(defn add-thing
  "Adds thing(s) to the world. Returns the new world."
  ([w id v]
   (let [v (validate-cell w id v)
         w2 (assoc-in w [:things id] v)]
     (if (identical? w w2)
       w
       (-> w2
           (sync-space id v)
           (sync-indexes v (get-thing w id))))))
  ([w id v & idvs]
   (reduce add-thing (add-thing w id v) (partition 2 idvs))))

(defn change-thing
  "Applies a function to the thing given by `id`. Returns the new world."
  ([w id f]
   (if-some [o (get-thing w id)]
     (add-thing w id (f o))
     w))
  ([w id f & args]
   (change-thing w id #(apply f % args))))

(defn remove-thing
  "Removes a thing from the world. Returns the new world."
  ([w id]
   (let [w2 (-> (dissoc-in w [:things id])
                (update :space space/remove id))]
     (if (identical? w w2)
       w
       (sync-indexes w2 (get-thing w id)))))
  ([w id & more]
   (reduce remove-thing w (cons id more))))

(defn world
  "Returns a new world, optionally initialized with the map of {id val} being
  the initial things."
  ([]
   {:things {}
    :space (space/space)
    :solid-index {}
    :opaque-index {}})
  ([tmap]
    (reduce-kv add-thing (world) tmap)))
