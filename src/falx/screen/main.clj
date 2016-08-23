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
            [falx.screen.main.entity :as main-entity]
            [falx.user :as user]
            [falx.db :as db]
            [falx.space :as space]))

(def mouse
  (ui/at-mouse
    (scene/fit sprite/mouse-point 32 32)))

(def resolution-default
  (user/default-settings ::user/setting.resolution))

(def cam-default
  (let [[w h] resolution-default]
    [0 0 w h]))

(def cell-size
  (user/setting ::user/setting.cell-size))

(defn render!
  [screen input rect]
  (let [db (::frame/db screen)
        user (::frame/user screen)
        [w h] (cell-size user)]
    (cam/view
      (::camera screen cam-default)
      (run! (fn [id] (main-entity/draw! (db/entity db id) w h))
            (space/iat-map db :limbo)))))

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