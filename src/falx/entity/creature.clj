(ns falx.entity.creature
  (:require [falx.entity :as entity]))

(def template
  {::entity/type ::entity/type.creature
   ::entity/layer ::entity/layer.creature
   ::entity/solid? true})

(defn select
  [cre]
  (assoc cre ::selected? true))

(defn unselect
  [cre]
  (dissoc cre ::selected?))