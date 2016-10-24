(ns falx.pos
  (:require [falx.db :as db]
            [falx.entity :as entity]))

(def schema
  {::cell {:index? true}
   ::slice {:index? true}
   ::level {:index? true}})

(defrecord Cell [level pt]
  Object
  (toString [this]
    (str "#cell [" level " " (nth pt 0) " " (nth pt 1) "]")))

(defrecord Slice [level layer]
  Object
  (toString [this]
    (str "#slice [" level " " layer "]")))

(defn at-cell
  [db cell]
  (db/q db ::cell cell))

(defn at-slice
  [db slice]
  (db/q db ::slice slice))

(defn at-level
  [db level]
  (db/q db ::level level))

(defn at
  [db pos]
  (condp instance? pos
    Cell (at-cell db pos)
    Slice (at-slice db pos)))

(defn cell
  [db eid]
  (db/attr db eid ::cell))

(defn slice
  [db eid]
  (db/attr db eid ::slice))

(defn level
  [db eid]
  (db/attr db eid ::level))

(defmulti layer entity/kind)

(defmethod layer :default
  [_ _]
  :falx.pos.layer/unknown)

(defn unput
  [db eid]
  (db/delete-attr db eid ::cell ::level ::slice))

(defn put
  [db eid cell]
  (db/merge-attr
    db eid
    {::cell cell
     ::level (:level cell)
     ::slice (->Slice (:level cell) (layer db eid))}))

(defmethod db/tx-op ::put
  [_ [_ eid cell] resolve]
  [[:db/fn put (resolve eid) cell]])

(defn put-tx
  [eid cell]
  [[::put eid cell]])

(defn unput-tx
  [eid]
  [[:db/delete eid ::cell]
   [:db/delete eid ::level]
   [:db/delete eid ::slice]])
