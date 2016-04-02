(ns falx.sub.ui
  (:require [falx.game :as g]
            [falx.event :as event]))

(defmulti derive-events* (fn [g event] (:type event)))

(defmethod derive-events* :default
  [g event])

(defn derive-events
  [g event]
  (let [f (fn ! [event] (if (event/multi? event)
                          (mapcat ! (:events event))
                          (cons event (mapcat ! (derive-events* g event)))))]
    (rest (f event))))

(defn publish-derived-events
  [g event]
  (reduce g/publish g (derive-events g event)))

(def subm
  {:event/display-changed [#'publish-derived-events]
   :event/button-hit [#'publish-derived-events]})

(defmethod derive-events* :event/display-changed
  [g {:keys [old-display display]}]
  (let [{:keys [size]} display
        {old-size :size} old-display]
    (when (not= old-size size)
      [(event/screen-size-changed old-size size)])))

(defmethod derive-events* :event/button-hit
  [g {:keys [button]}]
  (case button
    :left (for [a (g/having g :ui-rect)
                :let [[x y w h] (:ui-rect a)]
                :when (g/contains-mouse? g x y w h)]
            (event/ui-actor-clicked a))
    []))