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
  [g a x y]
  {:id          [::player (:player a)]
   :type        :actor/ui-box
   :context     {:color ui/light-gray}
   :rect        [x y player-panel-width player-panel-height]
   :ui-children (->> [(ui/actor (:id a) [0 3 64 64])]
                     (ui/relative-to x y))})

(defn get-player-panels
  [g x y]
  (for [n (range 6)
        :let [a (first (g/query g :player n))]
        :when a]
    (get-player-panel g a x (+ y (* n 2) (* n player-panel-height)))))

(def player-info-panel-width
  57)

(defn get-player-info-panel
  [g a x y]
  (let [id (:id a)
        w player-info-panel-width]
    {:id      [::player-info (:player a)]
     :type    :actor/ui-box
     :context {:color ui/light-gray}
     :rect    [x y player-info-panel-width player-panel-height]
     :ui-children
              (->> [(ui/stat-label id "hp" [2 1 w 14])
                    (ui/stat-label id "ap" [2 (+ 1 (* 15 1)) w 14])
                    (ui/stat-label id "pft" [2 (+ 1 (* 15 2)) w 14])
                    (ui/stat-label id "mft" [2 (+ 1 (* 15 3)) w 14])
                    (ui/stat-label id "mor" [2 (+ 1 (* 15 4)) w 14])]
                   (ui/relative-to x y))}))

(defn get-player-info-panels
  [g x y]
  (for [n (range 6)
        :let [a (first (g/query g :player n))]
        :when a]
    (get-player-info-panel g a x (+ y (* n 2) (* n player-panel-height)))))

(defn get-panel
  [g x y w h]
  {:id                                    ::panel
   :type                                  :element/panel
   :rect                                  [x y w h]
   :ui-root?                              true
   :ui-children                           (concat
                                            [(ui/pixel [x y w h] {:color ui/black})
                                             (ui/box [x y w h] {:color ui/gray})]
                                            (get-player-panels g (+ x 62) (+ y 3))
                                            (get-player-info-panels g (+ x 3) (+ y 3)))
   ;;handles
   [:handles? :event/screen-size-changed] true})

(defmethod g/uhandle [::panel :event/screen-size-changed]
  [g a {:keys [size]}]
  (assoc g :rect (get-rect (first size) (second size))))

(defn get-actors
  [g sw sh]
  (let [[x y w h] (get-rect sw sh)]
    [(get-panel g x y w h)]))