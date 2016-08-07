(ns falx.creature
  (:require [falx.entity :as entity]
            [falx.sprite :as sprite]
            [falx.draw :as d]
            [falx.db :as db]
            [falx.input :as input]))

(def base
  {::entity/type ::entity/type.creature
   ::entity/layer ::entity/layer.creature
   ::entity/solid? true})

(defn creature
  [id & {:as kvs}]
  (merge base
         kvs
         {:falx.db/id id}))

(defn players
  [gs]
  (db/query gs ::player? true))

(defn selected
  [gs]
  (db/query gs ::selected? true))

(defn selected-ids
  [gs]
  (db/iquery gs ::selected? true))

(defn select
  [gs id]
  (db/assert gs id ::selected? true))

(defn unselect
  [gs id]
  (db/retract gs id ::selected?))

(defn unselect-all
  [gs]
  (reduce unselect gs (selected-ids gs)))

(defn select-only
  [gs id]
  (-> (unselect-all gs)
      (select id)))

(defn click-player
  [gs e]
  (let [mod? (input/mod? gs)
        selected? (::selected? e)
        id (::db/id e)]
    (cond
      (and mod? selected?) (unselect gs id)
      mod? (select gs id)
      :else (select-only gs id))))

(defmethod entity/click ::entity/type.creature
  [gs e]
  (if (::player? e)
    (click-player gs e)
    gs))

(def player-selection
  (d/recolor sprite/selection [0 1 0 1]))

(defmethod entity/draw! ::entity/type.creature
  [c gs x y w h]
  (when (::selected? c)
    (d/draw! player-selection x y w h))
  (d/draw! sprite/human-male x y w h))

