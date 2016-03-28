(ns falx.ui.game
  (:require [falx.ui.game.right :as right]
            [falx.ui.game.bottom :as bottom]))

(defn viewport-camera
  [w h]
  {:type :camera/orthographic,
   :point [400.0 300.0],
   :size [w h],
   :flip-y? true})

(defn get-viewport
  [sw sh]
  (let [[x1 y1 w1 h1] (right/get-rect sw sh)
        [x2 y2 w2 h2] (bottom/get-rect sw sh)]
    {:id       ::viewport
     :type     :actor/viewport
     :camera   (viewport-camera sw sh)
     :ui-root? true
     :rect     [0 0 sw sh]}))

(defn get-actors
  [sw sh]
  (concat
    (right/get-actors sw sh)
    (bottom/get-actors sw sh)
    [(get-viewport sw sh)]))
