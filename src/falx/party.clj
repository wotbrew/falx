(ns falx.party
  (:require [falx.db :as db]
            [falx.entity.creature :as cre]))

(defn selected
  [db]
  (db/query db ::cre/selected? true))

(defn iselected
  [db]
  (db/iquery db ::cre/selected? true))

(defn select
  [db id]
  (db/alter db id cre/select))

(defn unselect
  [db id]
  (db/alter db id cre/unselect))

(defn reset-selected
  [db ids]
  (as-> db db
        (reduce unselect db (selected db))
        (reduce select db ids)))