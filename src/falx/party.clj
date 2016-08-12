(ns falx.party
  (:require [falx.db :as db]
            [falx.entity :as entity]
            [falx.creature :as creature]))


(defn level
  [db]
  (or (some ::entity/level (creature/selected db))
      (some ::entity/level (creature/players db))))