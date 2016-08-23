(ns falx.state.space
  (:require [falx.db :as db]
            [falx.entity :as entity]
            [falx.engine.point :as pt]))

(defn at
  [db pos]
  (db/query db ::entity/pos pos))

(defn iat
  [db pos]
  (db/iquery db ::entity/pos pos))

(defn at-slice
  [db slice]
  (db/query db ::entity/slice slice))

(defn iat-slice
  [db slice]
  (db/iquery db ::entity/slice slice))

(defn obstructions
  [db id pos]
  (let [e (db/entity db id)
        es (at db pos)]
    (filter #(entity/obstructs? % e) es)))

(defn can-put?
  [db id pos]
  (empty? (obstructions db id pos)))

(defn put
  ([db id pos]
   (if (can-put? db id pos)
     (db/alter db id entity/put pos)
     db))
  ([db id map pt]
   (put db id (entity/pos map pt)))
  ([db id map x y]
   (put db id (entity/pos map x y))))

(defn unput
  [db id]
  (db/alter db id entity/unput))

(defn map-pos
  [db id pt]
  (entity/map-pos (db/entity db id) pt))

(defn can-jump?
  ([db id pt]
   (can-put? db id (map-pos db id pt)))
  ([db id x y]
   (can-jump? db id [x y])))

(defn jump
  ([db id pt]
   (put db id (map-pos db id pt)))
  ([db id x y]
   (jump db id [x y])))

(defn pt
  [db id]
  (::entity/point (db/entity db id)))

(defn adj-pt?
  [db id pt]
  (if-some [pt2 (pt db id)]
    (pt/adj? pt pt2)
    false))

(defn can-step?
  ([db id pt]
   (and (can-jump? db id pt)
        (adj-pt? db id pt)))
  ([db id x y]
   (can-step? db id [x y])))

(defn step
  ([db id pt]
   (if (can-step? db id pt)
     (jump db id pt)
     db))
  ([db id x y]
   (step db id [x y])))