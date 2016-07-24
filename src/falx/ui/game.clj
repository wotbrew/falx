(ns falx.ui.game
  (:require [falx.ui :as ui]
            [falx.draw :as d]
            [falx.mouse :as mouse]
            [falx.scene :as scene]))

(defn drawfn
  [gs rect]
  (fn []
    (d/draw! (d/box) rect)))

(ui/defelem ::game
  :draw drawfn)

(def game-padding
  {:left (* 4 32)
   :top 0
   :right (* 4 32)
   :bottom (* 6 32)})

(ui/defelem ::mouse
  :view (fn [gs] (mouse/rect gs))
  :draw (fn [view _] (d/draw! d/mouse-point view)))

(ui/defscene ::scene
  (scene/stack
    [(scene/pad ::game game-padding)
     ::mouse]))