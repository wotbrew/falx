(ns falx.debug
  (:require [falx.state :as state]
            [falx.gdx :as gdx]
            [falx.gdx.mouse :as mouse]
            [falx.scene :as scene]
            [falx.ui :as ui]))

(def table
  (scene/maptable
    {"fps" gdx/fps
     "delta" gdx/delta-time
     "frame-id" gdx/frame-id
     "mouse" mouse/point}))