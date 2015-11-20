(ns falx.player
  (:require [falx.entity :as entity]))

(defn all
  [game]
  (entity/find-with game :player? true))

(defn find-first
  [game]
  (first (all game)))

(defn player
  [creature]
  (assoc creature :player? true))