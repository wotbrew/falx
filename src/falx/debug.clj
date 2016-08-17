(ns falx.debug
  (:require [falx.gdx :as gdx]
            [falx.engine.scene :as scene]
            [falx.engine.ui :as ui]
            [falx.engine.input :as input]
            [falx.engine.mouse :as mouse]
            [falx.engine.keyboard :as keyboard]))

(defn dinput
  [f]
  (ui/dynamic
    (fn [model input rect]
      (f input))))

(def dmouse #(dinput (comp %1 ::input/mouse)))
(def dkeyboard #(dinput (comp %1 ::input/keyboard)))

(def table
  (scene/htable
    (scene/fitw "fps" 64) gdx/fps
    "delta" gdx/delta-time
    "frameid" gdx/frame-id
    "mouse" (dmouse (juxt ::mouse/point ::mouse/pressed))
    "keyboard" (dkeyboard (juxt ::keyboard/pressed))))