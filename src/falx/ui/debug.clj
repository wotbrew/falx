(ns falx.ui.debug
  (:require [falx.frame :as frame]
            [falx.scene :as scene]
            [falx.ui.label :as label]))

(label/define ::fps
  :view ::frame/fps
  :text (partial str "fps: "))

(label/define ::delta
  :view ::frame/delta
  :text (partial str "delta: "))

(def scene
  (scene/frows
    16
    [::fps
     ::delta]))

