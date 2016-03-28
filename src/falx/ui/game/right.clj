(ns falx.ui.game.right
  (:require [falx.game :as g]
            [falx.ui :as ui]))

(defn get-rect
  [sw sh]
  [(- sw (* 4 32)) 0 (* 4 32) (- sh (* 4 32))])

(def player-panel-width
  64)

(def player-panel-height
  76)

(defn get-player-panel
  [index x y]
  {:id [::player index]
   :type :actor/ui-box
   :rect [x y player-panel-width player-panel-height]
   :ui-children [(ui/player-index index [0 3 64 64])]})

(defn get-player-panels
  [x y w h]
  (for [n (range 6)]
    (get-player-panel n x (+ y (* n 2) (* n player-panel-height)))))

(def player-info-panel-width
  57)

(defn get-player-info-panel
  [index x y]
  {:id [::player-info index]
   :type :actor/ui-box
   :rect [x y player-info-panel-width player-panel-height]})

(defn get-player-info-panels
  [x y w h]
  (for [n (range 6)]
    (get-player-info-panel n x (+ y (* n 2) (* n player-panel-height)))))

(defn get-panel
  [x y w h]
  {:id ::panel
   :type :element/panel
   :rect [x y w h]
   :ui-root? true
   :ui-children [(ui/pixel [0 0 w h] {:color ui/black})
                 (ui/box [0 0 w h] {:color ui/gray})
                 [::player 0]
                 [::player 1]
                 [::player 2]
                 [::player 3]
                 [::player 4]
                 [::player 5]
                 [::player-info 0]
                 [::player-info 1]
                 [::player-info 2]
                 [::player-info 3]
                 [::player-info 4]
                 [::player-info 5]]
   ;;handles
   [:handles? :event/screen-size-changed] true})

(defmethod g/uhandle [::panel :event/screen-size-changed]
  [g a {:keys [size]}]
  (assoc g :rect (get-rect (first size) (second size))))

(defn get-actors
  [sw sh]
  (let [[x y w h] (get-rect sw sh)]
    (concat [(get-panel x y w h)]
            (get-player-panels 60 3 w h)
            (get-player-info-panels 0 3 w h))))