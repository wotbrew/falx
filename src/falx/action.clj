(ns falx.action)

(defmulti action (fn [g action] (:type action)))

(defmethod action :default
  [g _]
  g)