(ns falx.creature
  (:require [falx.actor :as actor]))

(defn creature?
  [actor]
  (= (:type actor) :actor.type/creature))

(defn selectable?
  [creature]
  (creature? creature))

(defn can-select?
  [creature]
  (and (selectable? creature)
       (not (:selected? creature))))

(defn select
  [creature]
  (if (can-select? creature)
    (-> (assoc creature :selected? true)
        (actor/publish {:type :creature.event/selected
                        :creature creature}))
    creature))

(defn can-unselect?
  [creature]
  (and (selectable? creature)
       (:selected? creature)))

(defn unselect
  [creature]
  (if (can-unselect? creature)
    (-> (dissoc creature :selected?)
        (actor/publish {:type :creature.event/unselected
                        :creature creature}))
    creature))

(defn toggle-select
  [creature]
  (if (:selected? creature)
    (unselect creature)
    (select creature)))