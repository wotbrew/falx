(ns falx.debug
  (:require [falx.gdx :as gdx]
            [falx.engine.scene :as scene]
            [falx.engine.ui :as ui]
            [falx.engine.input :as input]
            [falx.engine.mouse :as mouse]
            [falx.engine.keyboard :as keyboard]
            [falx.frame :as frame]))

(defn dinput
  [f]
  (ui/dynamic
    (fn [model input rect]
      (f input))))

(def dmouse #(dinput (comp %1 ::input/mouse)))
(def dkeyboard #(dinput (comp %1 ::input/keyboard)))

(def table
  (->
    (scene/htable
      (scene/fitw "fps" 64) (ui/target ::frame/fps)
      "delta" (ui/target ::frame/delta)
      "frameid" (ui/target ::frame/frame-id)
      "mouse" (dmouse (juxt ::mouse/point ::mouse/pressed))
      "keyboard" (dkeyboard (juxt ::keyboard/pressed))
      "camera" (ui/target :falx.screen.main/camera)
      "wmouse" (ui/target (comp :falx.screen.main.world-mouse/cell :falx.screen.main/world-mouse))
      )
    (scene/fit 600 128)))