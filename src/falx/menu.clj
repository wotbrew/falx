(ns falx.menu
  (:require [falx.game :as game]
            [falx.geom :as g]
            [falx.draw :as draw]))

(def width 256)
(def height 256)

(defn rect
  [{::g/keys [w h]}]
  (g/rect
    (/ (- w width) 2)
    (/ (- h height) 2)
    width
    height))

(defn view
  [game]
  {::rect (rect (::game/screen-size game))})

(defn drawable
  [view]
  (-> [draw/box
       (draw/vspacing 32 ["New Game"
                          "Continue"
                          "Quit"])]
      (draw/fit (::rect view))))
