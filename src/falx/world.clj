(ns falx.world
  "Contains fundemental functions on the world and actors within the world."
  (:require [falx.db :as db]
            [falx.point :as point]))

;; ===
;; Basics

(defn query
  "Returns all the actors with the given k v pairs."
  ([w m]
   (db/query (:db w) m))
  ([w k v]
   (db/query (:db w) k v))
  ([w k v & kvs]
   (query w (into {k v} (partition 2 kvs)))))

(defn iquery
  "Returns all the actor ids with the given k v pairs."
  ([w m]
   (db/iquery (:db w) m))
  ([w k v]
   (db/iquery (:db w) k v))
  ([w k v & kvs]
   (iquery w (into {k v} (partition 2 kvs)))))

(defn get-actor
  "Returns the actor with the id."
  [w id]
  (db/get-entity (:db w) id))

(defn get-all
  "Returns all the actors in the world."
  [w]
  (db/get-all (:db w)))

;; ===
;; Slices

(defn slice
  "Returns a new slice.
  A slice represents a particular layer, in a particular level."
  [level layer]
  {:level level
   :layer layer})

;; ===
;; Cells

(defn cell
  "Returns a new cell, which is a point in a particular level."
  [level point]
  {:level level
   :point point})

(defn get-sibling-cell
  "Returns a cell which at the point
  sharing a level with the passed `cell`."
  [cell point]
  (assoc cell :point point))

(defn flood
  "Returns an infinite seq of cells out from the given cell.
  All the cells will be on the same level."
  [cell]
  (->> (point/flood (:point cell))
       (map (partial get-sibling-cell cell))))

(defn expand-cell
  "Returns a map of all the properties derived
  from a given cell and layer."
  [cell layer]
  {:cell cell
   :point (:point cell)
   :level (:level cell)
   :slice (slice (:level cell) layer)})

(defn get-at
  "Returns the actors at the given cell."
  [w cell]
  (query w :cell cell))

(defn iget-at
  "Returns the actor ids at the given cell."
  [w cell]
  (iquery w :cell cell))

(defn id-at?
  "Is the given actor at the cell (using the id)."
  [w id cell]
  (contains? (iget-at w cell) id))

;; ===
;; Default Cell

(def default-cell
  "The cell to use if no default-cell has been set explicitly."
  (cell :limbo [0 0]))

(defn get-default-cell
  "Returns the default cell of the map. Used for spawning new actors if no cell is defined."
  [w]
  (or (:default-cell w) default-cell))

(defn set-default-cell
  "Sets the default cell of the world to the given `cell`. Returns the new world."
  [w cell]
  (assoc w :default-cell cell))

;; ===
;; Solidity

(defn solid-at?
  "Is the given cell solid?"
  [w cell]
  (some :solid? (get-at w cell)))

(defn obstructs?
  "Is the given actor `a1` obstructed by `a2`?"
  [a1 a2]
  (and (:solid? a1) (:solid? a2)))

(defn obstructed-at?
  "Would the actor be obstructed at the given `cell`."
  [w a cell]
  (and (:solid? a) (solid-at? w cell)))

(defn flood-unobstructed
  "Returns a possibly infinite seq of cells via flood fill
  from the given."
  [w a cell]
  (->> (flood cell)
       (filter #(not (obstructed-at? w a %)))))

;; ===
;; Pathing

(defn get-path
  "Returns a path from the `actor` to `to-cell`."
  ([w a cell]
   (when-some [origin (:cell a)]
     (get-path w a origin cell)))
  ([w a from-cell to-cell]
   (->> (point/get-a*-path
          (fn [pt]
            (not (obstructed-at? w a (get-sibling-cell to-cell pt))))
          (:point from-cell)
          (:point to-cell))
        (map #(get-sibling-cell from-cell %)))))

;; ===
;; Changing actors

(defn- resolve-valid-cell
  [w a]
  (let [{:keys [id cell point level]} a
        target (cond cell cell
                     point (cell level point)
                     :else (get-default-cell w))]
    (if (id-at? w id target)
      target
      (first (flood-unobstructed w a target)))))

(defn add-actor
  "Adds an actor to a world, tries to add it to the closest available `cell`
  to the requested cell, or the default cell.
  Returns the new world."
  ([w a]
   (let [{:keys [layer]} a
         cell' (resolve-valid-cell w a)
         a' (conj a (expand-cell cell' layer))]
     (update w :db db/add-entity a')))
  ([w a & more]
   (reduce add-actor (add-actor w a) more)))

(defn add-actors
  "Adds a coll of actors to the world. Returns the new world."
  [w acoll]
  (reduce add-actor w acoll))

(defn update-actor
  "Applies a function `f` to the actor given by the id."
  ([w id f]
   (if-some [a (get-actor w id)]
     (add-actor w (f a))
     w))
  ([w id f & args]
   (update-actor w id #(apply f % args))))

(defn remove-actor
  "Completely removes an actor from the world."
  ([w id]
   (update w :db db/remove-entity id))
  ([w id & more]
   (reduce remove-actor (remove-actor w id) more)))

;; ===
;; Ctor

(defn world
  "Creates a new world."
  ([]
   {})
  ([acoll]
   (reduce add-actor (world) acoll)))