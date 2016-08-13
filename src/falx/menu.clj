(ns falx.menu
  (:require [falx.engine.ui :as ui]
            [falx.engine.scene :as scene]

            #_[falx.debug :as debug]
            [falx.sprite :as sprite]
            [falx.widget :as widget]))

(def mouse
  (ui/at-mouse (scene/fit sprite/mouse-point 32 32)))

(def scene
  (scene/stack
    #_(scene/fit #'debug/table 400 96)
    (scene/center
      (scene/rows
        (widget/button {:id ::button.new
                        :s "New"})
        (widget/button {:id ::button.continue
                        :s "Continue"})
        (widget/button {:id ::button.load
                        :s "Load"})
        (widget/button {:id ::button.options
                        :s "Options"})
        (widget/button {:id ::button.exit
                        :s "Exit"}))
      [320 320])
    mouse))