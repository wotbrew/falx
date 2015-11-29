(ns falx.time
  (:import (clojure.lang PersistentQueue)))

(def peace
  nil)

(defn combat
  [ids])

(defn peace?
  [time])

(defn combat?
  [time])

(defn add-to-combat
  [time id])

(defn remove-from-combat
  [time id])

(defn get-next-turn
  [time])

(defn get-acting
  [time])

(defn can-act?
  [time id]
  (or (peace? time)
      (= (get-acting time) id)))