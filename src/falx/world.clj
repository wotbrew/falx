(ns falx.world
  "Contains fundemental functions on the world and actors within the world."
  (:require [falx.db :as db]
            [falx.point :as point]))

;; ===
;; Basics

(defn query
  ([w m]
   (db/query (:db w) m))
  ([w k v]
   (db/query (:db w) k v))
  ([w k v & kvs]
   (query w (into {k v} (partition 2 kvs)))))

(defn iquery
  ([w m]
   (db/iquery (:db w) m))
  ([w k v]
   (db/iquery (:db w) k v))
  ([w k v & kvs]
   (iquery w (into {k v} (partition 2 kvs)))))

(defn get-actor
  [w id]
  (db/get-entity (:db w) id))

(defn get-all
  [w]
  (db/get-all (:db w)))

;; ===
;; Slices

(defn slice
  [level layer]
  {:level level
   :layer layer})

;; ===
;; Cells

(defn cell
  [level point]
  {:level level
   :point point})

(defn get-sibling-cell
  [cell point]
  (assoc cell :point point))

(defn flood
  [cell]
  (->> (point/flood (:point cell))
       (map (partial get-sibling-cell cell))))

(defn expand-cell
  [cell layer]
  {:cell cell
   :point (:point cell)
   :level (:level cell)
   :slice (slice (:level cell) layer)})

(defn get-at
  [w cell]
  (query w :cell cell))

(defn iget-at
  [w cell]
  (iquery w :cell cell))

(defn id-at?
  [w id cell]
  (contains? (iget-at w cell) id))

;; ===
;; Default Cell

(def default-cell
  (cell :limbo [0 0]))

(defn get-default-cell
  [w]
  (or (:default-cell w) default-cell))

(defn set-default-cell
  [w cell]
  (assoc w :default-cell cell))

;; ===
;; Solidity

(defn solid-at?
  [w cell]
  (some :solid? (get-at w cell)))

(defn obstructs?
  [a1 a2]
  (and (:solid? a1) (:solid? a2)))

(defn obstructed-at?
  [w a cell]
  (and (:solid? a) (solid-at? w cell)))

(defn flood-unobstructed
  [w a cell]
  (->> (flood cell)
       (filter #(not (obstructed-at? w a %)))))

;; ===
;; Pathing

(defn get-path
  [w from-cell to-cell]
  (->> (point/get-a*-path
         (fn [pt]
           (not (obstructed-at? w pt (get-sibling-cell to-cell pt))))
         (:point from-cell)
         (:point to-cell))
       (map #(get-sibling-cell from-cell %))))

;; ===
;; Changing actors

(defn- resolve-valid-cell
  [w a]
  (let [{:keys [id cell point level]} a
        target (cond cell cell
                     point (cell level point)
                     :else (get-default-cell w))]
    (if (id-at? w id target)
      target
      (first (flood-unobstructed w a target)))))

(defn add-actor
  ([w a]
   (let [{:keys [layer]} a
         cell' (resolve-valid-cell w a)
         a' (conj a (expand-cell cell' layer))]
     (update w :db db/add-entity a')))
  ([w a & more]
   (reduce add-actor (add-actor w a) more)))

(defn add-actors
  [w acoll]
  (reduce add-actor w acoll))

(defn update-actor
  ([w id f]
   (if-some [a (get-actor w id)]
     (add-actor w (f a))
     w))
  ([w id f & args]
   (update-actor w id #(apply f % args))))

(defn remove-actor
  ([w id]
   (update w :db db/remove-entity id))
  ([w id & more]
   (reduce remove-actor (remove-actor w id) more)))

;; ===
;; Ctor

(defn world
  [acoll]
  (reduce add-actor {} acoll))
