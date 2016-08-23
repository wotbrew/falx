(ns falx.screen.main.world-mouse
  (:require [falx.screen.main.camera :as cam]
            [falx.engine.mouse :as mouse]
            [falx.user :as user]
            [falx.engine.input :as input]
            [falx.engine.point :as pt]))

(def cell-size-setting
  (user/setting ::user/setting.cell-size))

(defn mouse
  [input user cam]
  (cam/view
    cam
    (let [mouse (::input/mouse input)
          pt (::mouse/point mouse [0 0])
          world-pt (cam/world-point cam pt)]
      {::point world-pt
       ::cell (pt/div world-pt (cell-size-setting user))})))