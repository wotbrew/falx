(ns falx.impl.selection
  (:require [falx.action :as action]
            [falx.space :as space]
            [falx.world :as world]))

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

(defn clear
  [g]
  (assoc-in g [:player :selected] #{}))

(defn selected?
  [g id]
  (contains? (:selected (:player g)) id))

(defn only-selected?
  [g id]
  (= (:selected (:player g)) #{id}))

(defn unselect
  [g id]
  (update-in g [:player :selected] disj id))

(defn select
  [g id]
  (update-in g [:player :selected] (fnil conj #{}) id))

(defn toggle-select
  [g id]
  (if (selected? g id)
    (if (only-selected? g id)
      g
      (unselect g id))
    (select g id)))

(defmethod action/action :select
  [g {:keys [target toggle? exclusive?]}]
  (if-some [targets (seq (resolve-targets g target))]
    (let [g (if exclusive?
              (clear g)
              g)]
      (->> targets
           (reduce (fn [g target]
                     (if toggle?
                       (toggle-select g target)
                       (select g target)))
                   g)))
    g))