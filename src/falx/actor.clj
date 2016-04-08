(ns falx.actor
  (:require [falx.position :as pos]))

(defn set-cell
  [a cell]
  (assoc a :cell cell
           :point (:point cell)
           :level (:level cell)
           :slice (pos/slice (:layer a) cell)))

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
