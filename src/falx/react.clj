(ns falx.react
  (:require [falx.util :as util]))

(defn unregister
  [m key]
  (if-some [event (-> m :key (get key))]
    (-> (util/dissoc-in m [:event event key])
        (util/dissoc-in [:key key]))
    m))

(defn register
  [m event-type key f]
  (-> (unregister m key)
      (assoc-in [:event event-type key] f)
      (assoc-in [:key key] event-type)))

(defn get-reactions
  [m event-type]
  (-> m :event (get event-type) vals))

(defn react
  [m event x]
  (let [handlers (get-reactions m (:type event))]
    (reduce #(%2 %1 event) x handlers)))

