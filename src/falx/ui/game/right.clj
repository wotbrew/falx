(ns falx.ui.game.right
  (:require [falx.game :as g]
            [falx.ui :as ui]
            [falx.rect :as rect]
            [falx.element :as e]))

(defn get-rect
  [sw sh]
  [(- sw (* 4 32)) 0 (* 4 32) (- sh (* 4 32))])

(def player-panel-width
  64)

(def player-panel-height
  76)

(defn player-panel-id
  [player]
  [::player player])

(defn get-player-panel
  [g a x y]
  {:id (player-panel-id (:player a))
   :type ::player-panel
   :elements [(e/on-hover (e/box [x y player-panel-width player-panel-height])
                          (e/highlighted-box [x y player-panel-width player-panel-height])
                          [x y player-panel-width player-panel-height])
              (e/actor (:id a) [x (+ y 3) 64 64])]})

(defn get-player-panels
  [g x y]
  (for [n (range 6)
        :let [a (first (g/query g :player n))]
        :when a]
    (get-player-panel g a x (+ y (* n 2) (* n player-panel-height)))))

(def player-info-panel-width
  57)

(defn player-info-panel-id
  [player]
  [::player-info player])

(defn get-player-info-panel
  [g a x y]
  (let [id (:id a)]
    {:id (player-info-panel-id (:player a))
     :type ::player-info
     :elements [(e/box [x y player-info-panel-width player-panel-height])]}))

(defn get-player-info-panels
  [g x y]
  (for [n (range 6)
        :let [a (first (g/query g :player n))]
        :when a]
    (get-player-info-panel g a x (+ y (* n 2) (* n player-panel-height)))))

(defn get-panel
  [g x y w h]
  {:id ::panel
   :type ::panel
   :ui-root? true
   :ui-children (concat (map #(player-panel-id %) (range 6))
                        (map #(player-info-panel-id %) (range 6)))
   :elements [(e/backing [x y w h])
              (e/box [x y w (inc h)])]})

(defn get-actors
  [g sw sh]
  (let [[x y w h] (get-rect sw sh)]
    (concat [(get-panel g x y w h)]
            (get-player-panels g (+ x 62) y)
            (get-player-info-panels g (+ x 3) y))))