(ns falx.frame
  (:require [falx.keys :as keys]
            [falx.mouse :as mouse]
            [falx.game :as game]
            [falx.size :as size]
            [falx.graphics.screen :as screen]))

(def default
  {::keys         keys/default
   ::screen-size  size/default
   ::screen-mouse mouse/default
   ::game         game/default})

(def get-screen-mouse ::screen-mouse)

(def get-keys ::keys)

(def get-game ::game)

(def get-screen-size ::screen-size)

(def frame (atom default))

(defn get-current
  []
  @frame)

(defn refresh-game
  [frame]
  (assoc frame ::game @game/game))

(defn refresh-screen
  [frame]
  (assoc frame ::screen-size (screen/get-size)))

(defn refresh-screen-mouse
  [frame]
  (update frame ::screen-mouse mouse/refresh))

(defn refresh
  [frame]
  (-> frame
      refresh-screen-mouse
      refresh-game))

(defn refresh!
  []
  (swap! frame refresh))