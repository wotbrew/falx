(ns falx.ui.game.bottom
  (:require [falx.game :as g]
            [falx.ui :as ui]
            [falx.element :as e]))

(defn get-rect
  [sw sh]
  [0 (- sh (* 4 32)) sw (* 4 32)])

(defn get-panel
  [x y w h]
  {:id       ::panel
   :type     ::panel
   :ui-root? true
   :elements  [(e/backing [x y w h])
               (e/box [x y w h])]})

(defn get-actors
  [g sw sh]
  (let [[x y w h] (get-rect sw sh)]
    [(get-panel x y w h)]))