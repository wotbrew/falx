(ns falx.graphics.widgets
  (:require [falx.graphics.image :as image]
            [falx.graphics.text :as text]))

(defprotocol IWidget
  (-draw! [this frame x y]))

(defprotocol IWidgetInput
  (-get-input-events [this frame x y]))

(extend-type Object
  IWidget
  (-draw! [this frame x2 y2])
  IWidgetInput
  (-get-input-events [this frame x2 y2]))

(defn get-input-events
  ([widget frame]
    (get-input-events widget frame 0 0))
  ([widget frame [x2 y2]]
   (get-input-events widget frame x2 y2))
  ([widget frame x y]
    (-get-input-events widget frame (+ x (:x widget)) (+ x (:y widget)))))

(defn draw!
  ([widget frame]
    (draw! widget frame 0 0))
  ([widget frame [x y]]
    (draw! widget frame x y))
  ([widget frame x y]
    (-draw! widget frame (+ x (:x widget)) (+ x (:y widget)))))

(defrecord Panel [x y coll]
  IWidget
  (-draw! [this frame x2 y2]
    (run! #(draw! % frame x2 y2) coll))
  IWidgetInput
  (-get-input-events [this frame x2 y2]
    (mapcat #(get-input-events % frame x2 y2) coll)))

(defn panel
  ([coll]
   (panel 0 0 coll))
  ([x y coll]
   (->Panel x y coll)))

(defrecord Image [x y w h sprite]
  IWidget
  (-draw! [this frame x2 y2]
    (image/draw! sprite x2 y2 w h)))

(defn image
  ([sprite]
    (image sprite 0 0))
  ([sprite x y]
    (image sprite x y (image/get-width sprite) (image/get-height sprite)))
  ([sprite x y w h]
    (->Image x y w h sprite)))

(defrecord DynamicText [x y text-fn]
  IWidget
  (-draw! [this frame x2 y2]
    (text/draw! (text-fn frame) x2 y2)))

(defn dynamic-text
  ([text-fn]
    (dynamic-text text-fn 0 0))
  ([text-fn x y]
    (->DynamicText x y text-fn)))

(defn static-text
  ([text]
    (static-text text 0 0))
  ([text x y]
    (dynamic-text (constantly text) x y)))

