(ns falx.party
  (:require [falx.db :as db]
            [falx.entity :as entity]
            [falx.creature :as creature]))


(defn slice
  [db]
  (or (some ::entity/slice (creature/selected db))
      (some ::entity/slice (creature/players db))))