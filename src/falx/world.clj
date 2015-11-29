(ns falx.world
  (:require [falx.space :as space]
            [falx.index :as index]))

(def default
  {:space space/default
   :index index/default})

;; ==============
;; ENTITIES

(defn find-entity
  [world id]
  (index/find (:index world) id))

(defn list-ids-in-cell
  [world cell]
  (space/list-in-cell (:space world) cell))

(defn list-entities-in-cell
  [world cell]
  (when-some [ids (seq (list-ids-in-cell world cell))]
    (map #(find-entity world %) ids)))

(defn list-ids-with
  [world attribute value]
  (index/list-ids-with (:index world) attribute value))

(defn list-with
  [world attribute value]
  (index/list-with (:index world) attribute value))

;; ============
;; SYNC

(defn sync-cell
  [world entity]
  (let [space (:space world)
        id (:id entity)
        current (space/find-cell space id)
        new (:cell entity)]
    (cond
      (nil? new) (update world :space space/unput id)
      (not= current new) (update world :space space/put id new)
      :else world)))

;; =============
;; CHANGE

(defn change
  [world entity]
  (let [m (update world :index index/ingest entity)
        id (:id entity (:id world 0))]
    (sync-cell m (find-entity m id))))

;; =============
;; QUERY

(defn ask
  ([world id f]
   (index/ask (:index world) id f))
  ([world id f & args]
    (ask world id #(apply f % args))))