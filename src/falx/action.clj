(ns falx.action
  (:require [clojure.tools.logging :refer [debug warn]]))

(defmulti action (fn [g action] (:type action)))

(defmethod action :default
  [g action]
  (warn "Unknown action:" action)
  g)