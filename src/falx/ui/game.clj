(ns falx.ui.game
  (:require [falx.ui.viewport :as viewport]
            [falx.ui.mouse :as mouse]))

(defn viewport-rect
  [x y w h]
  [x y w h])

(defn game-element
  [rect]
  (let [[x y w h] rect
        viewport-rect (viewport-rect x y w h)]
    {:type :game
     :rect rect
     :children [(viewport/viewport-element viewport-rect)
                (mouse/mouse-element [0 0 32 32])]}))