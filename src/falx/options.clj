(ns falx.options
  (:require [falx.engine.scene :as scene]
            [falx.engine.ui :as ui]
            [falx.sprite :as sprite]
            [falx.debug :as debug]
            [falx.engine.draw :as d]
            [falx.screen :as screen]))

(def mouse
  (ui/at-mouse (scene/fit sprite/mouse-point 32 32)))

(screen/defscene ::screen/id.options
  (scene/stack
    #'debug/table
    (scene/center
      (scene/htable
        (scene/fitw (d/center "Resolution [800 x 600]") 256)
        (screen/nav-button "change" {:goto-overlay ::screen/id.options.resolution})
        (scene/fitw (d/center "Fullscreen? [on]") 256)
        (screen/button "change"))
      [640 64])
    (screen/not-overlayed mouse)))

(screen/defscene ::screen/id.options.resolution
  (scene/stack
    (scene/center
      (scene/rows
        (screen/button "800 x 600")
        (screen/button "1024 x 768")
        (screen/button "1280 x 1024"))
      [320 320])
    mouse))