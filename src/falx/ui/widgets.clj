(ns falx.ui.widgets
  (:require [falx.graphics.image :as image]
            [falx.graphics.text :as text]
            [falx.graphics.shape :as shape]
            [falx.frame :as frame]
            [falx.mouse :as mouse]
            [falx.application :as app]))

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
    (-get-input-events widget frame (+ x (:x widget)) (+ y (:y widget)))))

(defn draw!
  ([widget frame]
    (draw! widget frame 0 0))
  ([widget frame [x y]]
    (draw! widget frame x y))
  ([widget frame x y]
    (-draw! widget frame (+ x (:x widget)) (+ y (:y widget)))))

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

(defn vertical-panel
  ([n coll]
   (vertical-panel n 0 0 coll))
  ([n x y coll]
   (panel x y (map-indexed (fn [i m] (update m :y (fnil + 0) (* i n))) coll))))

(defn horizontal-panel
  ([n coll]
   (horizontal-panel n 0 0 coll))
  ([n x y coll]
   (panel x y (map-indexed (fn [i m] (update m :x (fnil + 0) (* i n))) coll))))

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

(def fps-counter
  (dynamic-text (fn [_]
                  (app/get-fps))))

(defn static-text
  ([text]
    (static-text text 0 0))
  ([text x y]
    (dynamic-text (constantly text) x y)))

(defn centered-text
  ([text x y w h]
   (let [[x2 y2] (text/get-centered-point text x y w h)]
     (static-text text x2 y2))))

(defrecord Box [x y w h thickness]
  IWidget
  (-draw! [this frame x2 y2]
    (shape/draw-box! x2 y2 w h thickness)))

(defn box
  ([rect]
   (let [[x y w h] rect]
     (box x y w h)))
  ([x y w h]
    (box x y w h shape/*default-box-thickness*))
  ([x y w h thickness]
    (->Box x y w h thickness)))

(defn mouse-captured?
  [frame x y w h]
  (mouse/in-rectangle? (frame/get-screen-mouse frame) x y w h))

(defrecord ChangeDrawOnHover [x y w h widget f]
  IWidget
  (-draw! [this frame x2 y2]
    (if (mouse-captured? frame x2 y2 w h)
      (f frame x2 y2)
      (draw! widget frame x2 y2)))
  IWidgetInput
  (-get-input-events [this frame x2 y2]
    (get-input-events widget frame x2 y2)))

(defn change-color-on-hover
  ([widget color x y w h]
    (->ChangeDrawOnHover
      x y w h
      widget
      (fn [frame x2 y2]
        (image/with-color
          color
          (text/with-color
            color
            (draw! widget frame x2 y2)))))))

(defn text-button
  [text x y w h]
  (-> [(box 0 0 w h)
       (centered-text text 0 0 w h)]
      panel
      (change-color-on-hover :green x y w h)))

(defrecord OnClick [x y w h f]
  IWidgetInput
  (-get-input-events [this frame x2 y2]
    (when (and (mouse-captured? frame x2 y2 w h)
               (mouse/left-clicked? (frame/get-screen-mouse frame)))
      (let [r (f frame)]
        (if (sequential? r)
          r
          [r])))))

(defn on-click
  [f x y w h]
  (->OnClick x y w h f))

(defn button
  [x y w h & {:keys [text on-click-fn]}]
  (panel (concat [(text-button (or text "") x y w h)]
                 (when on-click-fn
                   [(on-click on-click-fn x y w h)]))))