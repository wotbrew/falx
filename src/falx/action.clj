(ns falx.action
  (:require [clojure.tools.logging :refer [debug warn]]))

(defmulti action (fn [g action] (:type action)))

(defmethod action :default
  [g action]
  (warn "Unknown action:" action)
  g)

(defn click
  [point modified?]
  {:type :click
   :point point
   :modified? modified?})

(defn alt-click
  [point modified?]
  {:type :alt-click
   :point point
   :modified? modified?})

(defn move-camera
  [direction speed]
  {:type :move-camera
   :direction direction
   :speed speed})

(defn handle-input
  [keyboard mouse]
  {:type :handle-input
   :mouse mouse
   :keyboard keyboard})

(defn move-mouse
  [point]
  {:type :move-mouse
   :point point})

(defn pass-time
  [ms]
  {:type :pass-time
   :ms ms})

(defn move
  [target destination]
  {:type :move
   :target target
   :destination destination})

(defn select
  [target {:keys [exclusive? toggle?]}]
  {:type :select
   :target target
   :exclusive? exclusive?
   :toggle? toggle?})