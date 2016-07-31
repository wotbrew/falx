(ns falx.debug
  (:require [falx.gdx :as gdx]
            [falx.gdx.mouse :as mouse]
            [falx.scene :as scene]))

(def table
  (scene/htable
    "fps" gdx/fps
    "delta" gdx/delta-time
    "frame-id" gdx/frame-id
    "mouse" mouse/point))