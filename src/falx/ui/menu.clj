(ns falx.ui.menu
  (:require [falx.ui.debug :as debug]
            [falx.scene :as scene]
            [falx.ui.button :as button]
            [falx.ui :as ui]
            [falx.draw :as d]
            [falx.point :as pt]
            [falx.mouse :as mouse]))

(button/define ::new
  :text "-  New  -"
  :click (fn [gs]
           (println "new!")
           gs))

(button/define ::continue
  :text "-  Continue  -")

(button/define ::options
  :text "-  Options  -")

(button/define ::exit
  :text "-  Exit  -")

(ui/defelem ::mouse
  :view (fn [gs] (mouse/rect gs))
  :draw (fn [view _] (d/draw! d/mouse-point view)))

(def scene
  (scene/stack
    [debug/scene
     (-> (scene/rows
           [::new
            ::continue
            ::options
            ::exit])
         (scene/center 480 320))
     ::mouse]))