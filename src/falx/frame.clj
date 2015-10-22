(ns falx.frame
  (:require [falx.keys :as keys]
            [falx.mouse :as mouse]))

(def default
  {::keys keys/default
   ::screen-mouse mouse/default})

(def get-screen-mouse ::screen-mouse)

(def get-keys ::keys)

(def frame (atom default))

(defn get-current
  []
  @frame)

(defn refresh-screen-mouse
  [frame]
  (update frame ::screen-mouse mouse/refresh))

(defn refresh
  [frame]
  (-> frame
      refresh-screen-mouse))

(defn refresh!
  []
  (swap! frame refresh))