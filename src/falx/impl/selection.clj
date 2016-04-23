(ns falx.impl.selection
  (:require [falx.action :as action]
            [falx.space :as space]
            [falx.world :as world]
            [clojure.set :as set]))

(def cell? map?)

(defn controlled?
  [g id]
  (contains? (:controlled (:player g)) id))

(defn get-at
  [g cell]
  (space/get-at (:space (:world g)) cell))

(defn resolve-targets
  [g x]
  (cond
    (cell? x) (for [id (get-at g x)
                    :when (controlled? g id)]
                id)
    (vector? x) (mapcat #(resolve-targets g %) x)
    :else [x]))

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