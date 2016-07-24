(ns falx.ui.debug
  (:require [falx.frame :as frame]
            [falx.scene :as scene]
            [falx.ui.label :as label]
            [falx.ui :as ui]))

(label/define ::fps
  :view ::frame/fps
  :text (partial str "fps: "))

(label/define ::delta
  :view ::frame/delta
  :text (partial str "delta: "))

(ui/defscene ::scene
  (scene/frows
    16
    [::fps
     ::delta]))
