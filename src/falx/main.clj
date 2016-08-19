(ns falx.main
  (:require [falx.screen :as screen]
            [falx.engine.scene :as scene]
            [falx.engine.ui :as ui]
            [falx.sprite :as sprite]
            [falx.debug :as debug]
            [falx.engine.ui.protocols :as proto]
            [falx.engine.camera :as cam]
            [falx.engine.draw :as d]
            [falx.engine.rect :as rect]))

(def mouse
  (ui/at-mouse
    (scene/fit sprite/mouse-point 32 32)))

(defn render!
  [screen input rect]
  (let [[cx cy] (::camera screen [0 0])
         [w h] (::size screen [800 600])]
    (cam/view
      [cx cy w h]
      (d/draw! sprite/human-male 0 0 32 32))))

(def main-view
  (reify proto/IDraw
    (-draw! [this screen input rect]
      (render! screen input rect))))

(screen/defscene ::screen/id.main
  (scene/stack
    main-view
    (scene/fit #'debug/table 400 96)
    mouse))