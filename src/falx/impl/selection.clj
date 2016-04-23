(ns falx.impl.selection
  (:require [falx.action :as action]
            [falx.space :as space]
            [falx.world :as world]
            [clojure.set :as set]))

(def cell? map?)

(defn can-select?
  [thing]
  true)

(defn resolve-targets
  [g x]
  (cond
    (cell? x) (for [id (space/get-at (:space (:world g)) x)
                    :when (can-select? (world/get-thing (:world g) id))]
                id)
    (vector? x) (mapcat #(resolve-targets g %) x)
    :else [x]))

(defn selected?
  [g id]
  (contains? (:selected (:player g)) id))

(defn toggle
  [existing new]
  (let [diff (set/difference existing new)]
    (cond
      (= diff existing) (set/union existing new)
      (and (= #{} diff) (seq existing)) #{(first existing)}
      :else diff)))

(defmethod action/action :select
  [g {:keys [target toggle? exclusive?]}]
  (if-some [targets (seq (resolve-targets g target))]
    (cond
      exclusive? (assoc-in g [:player :selected] (set targets))
      toggle? (update-in g [:player :selected] (fnil toggle #{}) targets)
      :else (update-in g [:player :selected] (fnil set/union #{}) targets))
    g))