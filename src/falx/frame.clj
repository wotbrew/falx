(ns falx.frame
  (:require [falx.keys :as keys]
            [falx.mouse :as mouse]))

(def default
  {::keys keys/default
   ::screen-mouse mouse/default})

(def get-screen-mouse ::screen-mouse)

(def get-keys ::keys)

(defn update-screen-mouse
  [frame]
  (update frame ::screen-mouse mouse/update-point))

(defn update-mouse
  [frame]
  (update-screen-mouse frame))

(def frame (atom default))

(defn get-current
  []
  @frame)

(defn update!
  []
  (swap! frame update-mouse))