(ns falx.sub.input
  (:require [falx.event :as event]
            [falx.game :as g]))

(defmulti derive-events* (fn [g event] (:type event)))

(defmethod derive-events* :default
  [g event])

(defmethod derive-events* :event/input-changed
  [g {:keys [old-input input]}]
  (let [{:keys [mouse keyboard]} input
        {old-mouse :mouse old-keyboard :keyboard} old-input]
    (cond->
      nil
      (not= old-mouse mouse) (conj (event/mouse-changed old-mouse mouse))
      (not= old-keyboard keyboard) (conj (event/keyboard-changed old-keyboard keyboard)))))

(defmethod derive-events* :event/mouse-changed
  [g {:keys [old-mouse mouse]}]
  (for [b (:hit mouse)
        :when (not (contains? (:hit old-mouse) b))]
    (event/button-hit b)))

(defmethod derive-events* :event/keyboard-changed
  [g {:keys [old-keyboard keyboard]}]
  (for [k (:hit keyboard)
        :when (not (contains? (:hit old-keyboard) k))]
    (event/key-hit k)))

(defn derive-events
  [g event]
  (let [f (fn ! [event] (cons event (mapcat ! (derive-events* g event))))]
    (rest (f event))))

(defn publish-derived-events
  [g event]
  (reduce g/publish g (derive-events g event)))

(def subm
  {:event/input-changed [#'publish-derived-events]})