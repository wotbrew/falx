(ns falx.main
  (:require [falx.screen :as screen]
            [falx.engine.scene :as scene]
            [falx.engine.ui :as ui]
            [falx.sprite :as sprite]
            [falx.debug :as debug]
            [falx.engine.ui.protocols :as proto]
            [falx.engine.draw :as d]
            [falx.frame :as frame]
            [falx.main.camera :as cam]))

(def mouse
  (ui/at-mouse
    (scene/fit sprite/mouse-point 32 32)))

(defn render!
  [screen input rect]
  (cam/view
    (::camera screen [0 0])
    (::screen/size screen [800 600])
    (d/draw! sprite/human-male 0 0 32 32)))

(defn handle
  [screen input]
  (let [user (::frame/user screen)
        delta (::frame/delta screen)]
    (-> screen
        (update ::camera cam/handle input user delta))))

(def main-view
  (reify proto/IDraw
    (-draw! [this screen input rect]
      (render! screen input rect))
    proto/IHandle
    (-handle [this screen input rect]
      (handle screen input))))

(screen/defscene ::screen/id.main
  (scene/stack
    main-view
    (scene/fit #'debug/table 400 96)
    mouse))