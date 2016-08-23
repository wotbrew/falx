(ns falx.entity.creature
  (:require [falx.entity :as entity]))

(defn create
  [& {:as kvs}]
  (entity/create ::entity/type.creature kvs))

(defn select
  [cre]
  (assoc cre ::selected? true))

(defn unselect
  [cre]
  (dissoc cre ::selected?))