(ns falx.menu
  (:require [falx.engine.ui :as ui]
            [falx.engine.scene :as scene]

            [falx.debug :as debug]
            [falx.sprite :as sprite]
            [falx.screen :as screen]))

(def mouse
  (ui/at-mouse (scene/fit sprite/mouse-point 32 32)))

(screen/defscene
  ::screen/id.menu
  (scene/stack
    (scene/fit #'debug/table 400 96)
    (scene/center
      (scene/rows
        (screen/nav-button "New" {:goto ::screen/id.main})
        (screen/nav-button "Continue" {:goto ::screen/id.main})
        (screen/button "Load")
        (screen/nav-button "Options" {:goto ::screen/id.options})
        (screen/button "Exit"))
      [320 320])
    mouse))
