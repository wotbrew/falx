(ns falx.state
  (:require [falx.gdx :as gdx]))

(def signals
  (atom {}))

(defmacro defsignal
  [k signal]
  `(let [k# ~k]
     (derive k# ::signal)
     (swap! signals assoc k# ~signal)
     nil))

(defn signal
  [k]
  (get @signals k))

(def frame
  (gdx/signal
    (reduce-kv (fn [gs k v]
                 (assoc gs k @v))
               {}
               @signals)))


