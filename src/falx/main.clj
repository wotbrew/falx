(ns falx.main
  (:require [falx.scene :as scene]
            [falx.debug :as debug]
            [falx.ui :as ui]))

(def key-bindings
  {})

(def scene*
  (scene/stack
    (scene/fit #'debug/table 400 72)))

(defn handle-all
  [gs rect]
  gs)

(def scene
  (ui/behaviour
    scene*
    handle-all))