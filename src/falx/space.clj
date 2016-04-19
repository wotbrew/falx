(ns falx.space
  "Contains operations on space and spacial concepts such as cells, levels, layers, slices.

  Space is discretized into cells, each cell representing a point on a level. The level is assumed to be symbolic, e.g \"map-1\"

  Each cell can contain one or more identities, which are assumed to be symbolic representations of something else, e.g
  :fred. One can manipulate the position of identities directly with `move`, or `change-positions`.
  Each identity can be in only one cell at a time.

  The pairing of an identity positioned in space is called an `object`.
  Each object in the space can be placed into a particular layer. A layer on a particular level is called a `slice`.
  Add objects with `add-object`."
  (:require [falx.db :as db]
            [falx.point :as point])
  (:refer-clojure :exclude [remove]))

(defn cell
  "Returns a cell."
  ([level point]
   {:level level
    :point point})
  ([level x y]
   (cell level [x y])))

(defn translate
  "Returns a cell at the given point, translated from the given reference cell."
  [cell point]
  (assoc cell :point point))

(defn translate-all
  "Translates a coll of points using the given reference cell. Returns a the seq of cells."
  [cell points]
  (map (partial translate cell) points))

(defn slice
  "Returns a slice."
  [level layer]
  {:level level
   :layer layer})

(defn object
  "Returns an object, an object represents a positioned identity. You can add objects to space
  with `add-object`."
  ([id cell]
   (object id nil cell))
  ([id layer cell]
   {:id id
    :layer layer
    :cell cell
    :slice (slice (:level cell) layer)})
  ([id layer level point]
   (object id layer (cell level point)))
  ([id layer level x y]
   (object id layer level [x y])))

(defn get-object
  "Returns the object for the given identity in space."
  [space id]
  (db/get-entity space id))

(defn get-cell
  "Returns the cell (if any) containing the given identity."
  [space id]
  (:cell (get-object space id)))

(defn get-all
  "Returns all the identities in space or any subset thereof.
  `m` can contain keys {:level, :slice, :layer, :cell} to limit the search."
  ([space]
   (db/get-all-ids space))
  ([space m]
   (db/iquery space m)))

(def get-at
  "Returns the identities in the given cell."
  (db/iquery-fn :cell))

(def get-at-slice
  "Returns the identities in the given slice."
  (db/iquery-fn :slice))

(def get-at-layer
  "Returns the identities in the given layer."
  (db/iquery-fn :layer))

(def get-at-level
  "Returns the identities in the given level."
  (db/iquery-fn :level))

(defn get-at-every
  "Returns the identities in the given coll of cells."
  [space cell-coll]
  (mapcat #(get-at space %) cell-coll))

(defn- apply-to-object
  ([space id f]
   (when-some [o (get-object space id)]
     (f o)))
  ([space id f & args]
   (apply-to-object space id #(f % args))))

(defn- apply-to-cell
  ([space id f]
   (when-some [cell (get-cell space id)]
     (f cell)))
  ([space id f & args]
   (apply-to-cell space id #(f % args))))

(defn get-adjacent-cells
  "Returns all cells adjacent to either
   - The given `cell`.
   - The identity `id` in `space`."
  ([cell]
   (translate-all cell (point/get-adjacent (:point cell))))
  ([space id]
   (apply-to-cell space id get-adjacent-cells)))

(defn get-cardinal-adjacent-cells
  "Returns all cells (cardinally) adjacent to either
   - The given `cell`.
   - The identity `id` in `space`."
  ([cell]
   (translate-all cell (point/get-cardinal-adjacent (:point cell))))
  ([space id]
   (apply-to-cell space id get-cardinal-adjacent-cells)))

(defn flood
  "Returns an infinite sequence of cells via flood fill from either
   - The given `cell`
   - The identity `id` in `space`."
  ([cell]
   (translate-all cell (point/flood (:point cell))))
  ([space id]
   (apply-to-cell space id flood)))

(defn add-object
  "Adds an object(s) to space. Create an object with `object`."
  ([space obj]
   (db/add space obj))
  ([space obj & more]
   (reduce add-object (add-object space obj) more)))

(defn remove
  "Removes the `id` from space."
  ([space id]
   (db/delete space id))
  ([space id & ids]
   (reduce remove (remove space id) ids)))

(defn move
  "Moves the identity in space. If it is not already in space, it will be added."
  ([space id cell]
   (let [x (db/change space id assoc :cell cell)]
     (if (identical? x space)
       (move space id nil cell)
       x)))
  ([space id layer cell]
   (add-object space (object id layer cell)))
  ([space id layer level point]
   (move space id layer (cell level point)))
  ([space id layer level x y]
   (move space id layer level [x y])))

(defn position-map->objects
  [position-map]
  (for [[level layers] position-map
        [layer points] layers
        [point ids] points
        id ids]
    (object id layer level point)))

(defn change-positions
  "Changes positions of identities in space according to a position map.
  i.e
   `{level {layer {point #{id}}}`"
  ([space position-map]
   (reduce add-object space (position-map->objects position-map)))
  ([space level layer-map]
   (change-positions space {level layer-map}))
  ([space level layer point-map]
   (change-positions space {level {layer point-map}}))
  ([space level layer point ids]
   (change-positions space {level {layer {point ids}}})))

(defn space
  "Returns a new space value, optinally initialized via the given `position-map`.
  i.e
   `{level {layer {point #{id}}}`"
  ([]
   (db/db))
  ([position-map]
   (change-positions (space) position-map)))