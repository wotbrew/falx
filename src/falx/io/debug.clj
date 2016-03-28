(ns falx.io.debug)

(defmulti event! :type)

(defmethod event! :default
  [event]
  (when (:debug? event)
    (println event)))