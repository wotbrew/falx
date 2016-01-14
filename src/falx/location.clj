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

(defn adjacent?
  [cell-a cell-b]
  (and (= (:level cell-a) (:level cell-b))
       (point/adjacent? (:point cell-a) (:point cell-b))))