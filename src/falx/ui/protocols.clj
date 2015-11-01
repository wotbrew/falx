(ns falx.ui.protocols
  (:require [falx.rect :as rect]
            [falx.frame :as frame]
            [falx.mouse :as mouse]))

(defprotocol IWidget
  (-draw! [this x y]))

(defprotocol IWidgetInput
  (-get-input-events [this x y]))

(extend-type Object
  IWidget
  (-draw! [this x2 y2])
  IWidgetInput
  (-get-input-events [this x2 y2]))

(defn get-input-events
  ([widget]
   (get-input-events widget 0 0))
  ([widget [x2 y2]]
   (get-input-events widget x2 y2))
  ([widget x y]
   (-get-input-events widget (+ x (:x widget)) (+ y (:y widget)))))

(defn draw!
  ([widget]
   (draw! widget 0 0))
  ([widget [x y]]
   (draw! widget x y))
  ([widget x y]
   (-draw! widget (+ x (:x widget)) (+ y (:y widget)))))

(defn mouse-captured?
  ([frame rect]
    (let [[x y w h] rect]
      (mouse-captured? frame x y w h)))
  ([frame x y w h]
    (rect/contains-point? x y w h (mouse/get-point (frame/get-screen-mouse frame)))))