(ns falx.location
  (:require [falx.point :as point]))

(defn cell
  "The cell represents a point in the world, each point
  can be located at a `level`."
  [level point]
  {:level level
   :point point})

(defn slice
  "The slice represents a layer in the world, each layer
  can be located at a `level`."
  [level layer]
  {:level level
   :layer layer})

(defn same-level?
  [cell-a cell-b]
  (= (:level cell-a) (:level cell-b)))

(defn adjacent?
  "Are the 2 cells adjacent to one another?"
  [cell-a cell-b]
  (and (same-level? cell-a cell-b)
       (point/adjacent? (:point cell-a) (:point cell-b))))

(defn get-adjacent
  "Gets the adjacent cells"
  [cell]
  (->> (point/get-adjacent (:point cell))
       (map (partial falx.location/cell (:level cell)))))

(defn get-distance
  "Gets the distance between 2 cells, MAX_VALUE if they are on different levels"
  [cell-a cell-b]
  (if-not (same-level? cell-a cell-b)
    Double/MAX_VALUE
    (point/get-manhattan-distance (:point cell-a) (:point cell-b))))

(defn get-nearest
  "Returns the nearest cell to `cell` in `cells`."
  [cell cells]
  (first (sort-by #(get-distance cell %) cells)))