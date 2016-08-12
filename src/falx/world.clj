(ns falx.world
  (:require [falx.db :as db]
            [falx.game :as g]
            [falx.entity :as entity]))

(defn world
  []
  {})

(defn entity
  ([gs id]
   (db/entity gs id)))

(defn put
  ([gs id pos]
   (db/alter gs id entity/put pos))
  ([gs id level pt]
   (db/alter gs id entity/put level pt)))

(defn step
  ([gs id pt]
   (db/alter gs id entity/step pt))
  ([gs id x y]
   (step gs id [x y])))

(defn add
  [gs m]
  (let [id (inc (::id-seed gs -1))]
    (-> gs
        (db/add (assoc m ::db/id id))
        (assoc ::id-seed id))))