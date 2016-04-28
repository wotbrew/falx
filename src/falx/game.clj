(ns falx.game
  (:require [falx.db :as db]
            [clojure.data.priority-map :as pm]
            [falx.action :as action]))

(defn game
  [ecoll]
  {:tick 0
   :active nil
   :next-id 0
   :db (db/db ecoll)
   :schedule (pm/priority-map)})

(defn player?
  [id-or-m]
  (if (map? id-or-m)
    (= :player (:id id-or-m))
    (= :player id-or-m)))

(defn active?
  ([g]
   (some? (:active g)))
  ([g id]
   (= (:active g) id)))

(defn player-active?
  [g]
  (active? g :player))

(defn add-entity
  [g entity]
  (update g :db db/add entity))

(defn get-entity
  [g id]
  (-> g :db (db/get-entity id)))

(defn get-point
  [g id]
  (:point (get-entity g id)))

(defn update-entity
  ([g id f]
   (update g :db db/change id f))
  ([g id f & args]
   (update-entity g id #(apply f % args))))

(defn get-of-type
  [g type]
  (db/query (:db g) :type type))

(defn get-at
  [g point]
  (db/query (:db g) :point point))


(defn iget-at-layer
  [g layer]
  (db/iquery (:db g) :layer layer))

(defn get-at-layer
  [g layer]
  (db/query (:db g) :layer layer))

(defn get-player
  [g]
  (get-entity g :player))

(defn say
  [g s]
  (if (= s (peek (:log g)))
    g
    (update g :log (fn [log]
                     (if (< (count log) 10)
                       (conj (or log []) s)
                       (into [] cat [(drop 5 log)
                                     [s]]))))))

(action/bind :say #'say)

(defn schedule-at
  [g action tick]
  (update g :schedule assoc action tick))

(defn schedule-in
  [g action in-ticks]
  (schedule-at g action (+ in-ticks (:tick g 0))))

(defn peek-action
  [g]
  (peek (:schedule g)))

(defn pop-action
  [g]
  (update g :schedule pop))

(defn selected?
  [g n]
  (= (:selected g) n))

(defn select-creature
  [g n]
  (if (< n (count (:creatures (get-player g))))
    (assoc g :selected n)
    g))

(defn get-selected
  [g]
  (when-some [n (:selected g)]
    (when (< n (count (:creatures (get-player g))))
      (get-in (get-player g) [:creatures n]))))

(action/bind :select-creature #'select-creature)