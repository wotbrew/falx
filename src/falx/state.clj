(ns falx.state
  (:require [falx.gdx :as gdx]))

(defonce signals
  (atom {}))

(defn defsignal
  [k signal & {:keys [merge]}]
  (derive k ::signal)
  (swap! signals assoc k {:signal signal
                          :merge (or merge (fn [a b] b))})
  nil)

(defn signal
  [k]
  (:signal (get @signals k)))

(def frame
  (gdx/signal
    (reduce-kv (fn [m k {:keys [signal]}]
                 (assoc m k @signal))
               {}
               @signals)))

(defn splice
  [gs frame]
  (reduce-kv (fn [gs k {:keys [merge]}]
               (if (contains? gs k)
                 (update gs k merge (get frame k))
                 (assoc gs k (get frame k))))
             gs
             @signals))

