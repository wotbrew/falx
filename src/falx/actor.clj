(ns falx.actor
  (:require [falx.position :as pos]))

(defn set-pos
  [a cell]
  (assoc a :cell cell
           :point (:point cell)
           :level (:level cell)
           :slice (pos/slice (:layer a) cell)))

(defn rem-pos
  [a]
  (dissoc a :cell :point :level :slice))