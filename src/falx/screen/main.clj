(ns falx.screen.main
  (:require [falx.screen :as screen]
            [falx.engine.scene :as scene]
            [falx.engine.ui :as ui]
            [falx.sprite :as sprite]
            [falx.debug :as debug]
            [falx.engine.ui.protocols :as proto]
            [falx.engine.draw :as d]
            [falx.frame :as frame]
            [falx.screen.main.camera :as cam]
            [falx.screen.main.world-mouse :as world-mouse]
            [falx.user :as user]))

(def mouse
  (ui/at-mouse
    (scene/fit sprite/mouse-point 32 32)))

(def resolution-default
  (user/default-settings ::user/setting.resolution))

(defn render!
  [screen input rect]
  (let [size (::screen/size screen resolution-default)
        [w h] size
        [cx cy] (::camera screen [0 0])]
    (cam/view
      [cx cy w h]
      (d/draw! sprite/human-male 0 0 32 32))))

(defn handle
  [screen input]
  (let [user (::frame/user screen)
        delta (::frame/delta screen)
        cam (-> screen ::camera (cam/handle input user delta))]
    (assoc screen
      ::camera cam
      ::world-mouse (world-mouse/mouse input user cam))))

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
    #'debug/table
    mouse))