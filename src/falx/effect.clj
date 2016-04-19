(ns falx.effect
  (:require [clojure.tools.logging :refer [debug warn]]))

(defmulti effect! :type)

(defmethod effect! :default
  [effect]
  (warn "Undefined effect: " effect))

(defn debug-value
  [v]
  {:type :debug-value
   :v v})