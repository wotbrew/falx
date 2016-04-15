(ns falx.actor
  (:require [falx.position :as pos]
            [falx.util :as util]))

(defn set-cell
  [a cell]
  (assoc a :cell cell
           :point (:point cell)
           :level (:level cell)
           :slice (pos/slice (:layer a) (:level cell))))

(defn rem-cell
  [a]
  (dissoc a :cell :point :level :slice))

(defn creature?
  [a]
  (= (:type a) :actor/creature))

(defn player?
  [a]
  (some? (:player a)))

(defn can-select?
  [a]
  (and (creature? a) (player? a)))

(defn select
  [a]
  (if (can-select? a)
    (assoc a :selected? true)
    a))

(defn unselect
  [a]
  (dissoc a :selected?))

(defn toggle-selection
  [a]
  (update a :selected? (comp boolean not)))

(defn obstructs?
  [a1 a2]
  (and (:solid? a1) (:solid? a2)))

(defn path-to
  [a cell]
  (assoc a :pathing-to cell
           :activity :pathing))

(defn walk-to
  [a cell]
  (-> (dissoc a :path)
      (path-to cell)))

(defn walking-to?
  [a cell]
  (= (:walking-to a) cell))

(defn can-walk?
  [a]
  (some? (:cell a)))