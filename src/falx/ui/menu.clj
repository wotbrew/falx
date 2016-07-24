(ns falx.ui.menu
  (:require [falx.ui.debug :as debug]
            [falx.draw :as d]
            [falx.ui :as ui]
            [falx.scene :as scene]
            [falx.ui.button :as button]))

(button/define ::new
  :text "New")

(button/define ::continue
  :text "Continue")

(button/define ::options
  :text "Options")

(button/define ::exit
  :text "Exit")

(def scene
  (scene/stack
    [debug/scene
     (-> (scene/rows [::new
                      ::continue
                      ::options
                      ::exit])
         (scene/center 480 320))]))