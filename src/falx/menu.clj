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
      (scene/rows
        (ui/button "New")
        (ui/button "Continue")
        (ui/button "Options")
        (ui/button "Exit"))
      [320 320])))