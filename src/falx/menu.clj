(ns falx.menu
  (:require [falx.ui :as ui]
            [falx.draw :as d]
            [falx.scene :as scene]
            [falx.gdx.mouse :as mouse]
            [falx.gdx :as gdx]
            [falx.debug :as debug]))

(def scene
  (scene/stack
    (scene/fit #'debug/table 192 72)
    (scene/center
      (d/box)
      [320 320])))