(ns falx.ui
  (:require [falx.rect :as rect]))

(defn get-state
  [g element]
  (-> g :ui (get (:id element))))

(defmulti handle-click (fn [g element point] (:type element)))

(defmethod handle-click :default
  [g element point]
  g)

(defmulti handle-alt-click (fn [g element point] (:type element)))

(defmethod handle-alt-click :default
  [g element point]
  g)

(defmulti handle-mod-click (fn [g element point] (:type element)))

(defmethod handle-mod-click :default
  [g element point]
  g)

(defn click
  [g element point]
  (let [rect (:rect element)]
    (if (and rect (rect/contains-point? rect point))
      (as-> g g
            (handle-click g element point)
            (reduce #(click %1 %2 point) g (:children element)))
      g)))

(defn alt-click
  [g element point]
  (let [rect (:rect element)]
    (if (and rect (rect/contains-point? rect point))
      (as-> g g
            (handle-alt-click g element point)
            (reduce #(alt-click %1 %2 point) g (:children element)))
      g)))

(defn mod-click
  [g element point]
  (let [rect (:rect element)]
    (if (and rect (rect/contains-point? rect point))
      (as-> g g
            (handle-mod-click g element point)
            (reduce #(mod-click %1 %2 point) g (:children element)))
      g)))