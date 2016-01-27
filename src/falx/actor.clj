(ns falx.actor
  (:require [falx.react :as react]
            [falx.position :as pos]
            [falx.util :as util])
  (:refer-clojure :exclude [empty]))

(def empty
  {:events []
   :reactions {}})

(defn actor
  [id m]
  (merge empty
         m
         {:id id}))

(defn split-events
  [actor]
  (let [a (assoc actor :events [])]
    {:actor  a
     :events (mapv #(assoc % :actor a) (:events actor))}))

(defn publish
  [actor event]
  (let [actor' (react/react actor (:reactions actor) event)]
    (update actor' :events conj event)))

(defn at-cell?
  [actor cell]
  (= (:cell actor) cell))

(defn at-point?
  [actor point]
  (= (:point actor) point))

(defn at-level?
  [actor level]
  (= (:level actor) level))

(defn obstructs?
  [a1 a2]
  ;;execute this way round for common case speed
  (and (:solid? a2) (:solid? a1)))

(defn get-obstructions
  [actor acoll]
  (filter #(obstructs? actor %) acoll))

(defn some-obstruction
  [actor acoll]
  (first (get-obstructions actor acoll)))

(defn some-obstructs?
  [actor acoll]
  (boolean (some-obstruction actor acoll)))

(defn adjacent-to-cell?
  [actor cell]
  (when-some [c (:cell actor)]
    (pos/adjacent? c cell)))

(defn adjacent-to-actor?
  [a1 a2]
  (when-some [c (:cell a2)]
    (pos/adjacent? a1 c)))

(defn- change-position
  [actor cell]
  (if (at-cell? actor cell)
    actor
    (assoc actor
      :cell cell
      :point (:point cell)
      :level (:level cell)
      :slice (pos/slice (:layer actor) (:level cell)))))

(defn put-event
  [old-cell cell]
  {:type :actor.event/put
   :old-cell old-cell
   :cell cell})

(defn unput-event
  [cell]
  {:type :actor.event/unput
   :cell cell})

(defn put
  [actor cell]
  (let [a (change-position actor cell)
        old-cell (:cell actor)
        moved? (not= old-cell (:cell a))]
    (cond->
      a
      moved? (publish (put-event old-cell cell))
      old-cell (publish (unput-event cell)))))

(defn unput
  [actor]
  (if (some? (:cell actor))
    (-> (dissoc actor :cell :point :level :slice)
        (publish (unput-event (:cell actor))))
    actor))

(defn can-step?
  [actor cell]
  (adjacent-to-cell? actor cell))

(defn step
  [actor cell]
  (if (can-step? actor cell)
    (put actor cell)
    actor))