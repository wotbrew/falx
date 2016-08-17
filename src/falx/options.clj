(ns falx.options
  (:require [falx.engine.scene :as scene]
            [falx.engine.ui :as ui]
            [falx.sprite :as sprite]
            [falx.debug :as debug]
            [falx.engine.draw :as d]
            [falx.screen :as screen]))

(def mouse
  (ui/at-mouse (scene/fit sprite/mouse-point 32 32)))

(def scene
  (scene/stack
    (scene/fit #'debug/table 400 96)
    (scene/center
      (scene/htable
        (scene/fitw (d/center "Resolution [800 x 600]") 256)
        (screen/button "change")
        (scene/fitw (d/center "Fullscreen? [on]") 256)
        (screen/button "change"))
      [640 64])
    mouse))

(screen/defscene ::screen/id.options scene)