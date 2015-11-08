(ns falx.widget
  (:require [falx.graphics :as graphics]
            [falx.rect :as rect]
            [falx.mouse :as mouse]
            [clj-gdx :as gdx]
            [falx.sprite :as sprite]))


(defmulti draw-widget! :type)

(defmethod draw-widget! :default
  [m]
  (graphics/draw-in! m (:rect m rect/default)))

(defmulti update-widget* (fn [m game] (:type m)))

(defn update-widget
  [m game]
  (cond->
    m
    (:on-update m) ((:on-update m) game)
    :always (update-widget* game)))

(defmethod update-widget* :default
  [m _]
  m)

(defmulti get-input-events (fn [m game] (:type m)))

(defmethod get-input-events :default
  [_ _]
  nil)

(defn panel
  [coll]
  {:type :ui/panel
   :coll coll})

(defmethod draw-widget! :ui/panel
  [m]
  (run! draw-widget! (:coll m)))

(defmethod update-widget* :ui/panel
  [m game]
  (update m :coll (partial mapv #(update-widget % game))))

(defmethod get-input-events :ui/panel
  [m game]
  (mapcat #(get-input-events % game) (:coll m)))

(defn label
  [rect text]
  {:type :ui/label
   :rect rect
   :text text
   :context {}})

(defmethod draw-widget! :ui/label
  [m]
  (graphics/draw-centered-string! (:rect m) (:text m) (:context m)))

(defn debug-label
  [rect f]
  (assoc (label rect "Initializing...")
    :on-update (fn [m game]
                 (assoc m :text (f game)))))

(defn fps-label
  [rect]
  (debug-label rect #(str "fps: " (:fps % 0))))

(defn box
  [rect]
  {:type :ui/box
   :rect rect
   :context {}})

(defmethod draw-widget! :ui/box
  [m]
  (graphics/draw-box! (:rect m) (:context m)))

(defn text-button
  [text rect]
  {:type :ui/text-button
   :text text
   :rect rect})

(defmethod draw-widget! :ui/text-button
  [m]
  (let [{:keys [rect text]} m
        rect (or rect rect/default)]
    (cond
      (:disabled? m) (graphics/draw-disabled-text-button! rect text)
      (:highlighted? m) (graphics/draw-highlighted-text-button! rect text)
      :else (graphics/draw-text-button! rect text))))

(defmethod update-widget* :ui/text-button
  [m game]
  (cond
    (:disabled? m) m
    :else
    (assoc m :highlighted? (mouse/in? (:mouse game) (:rect m rect/default)))))

(defmethod get-input-events :ui/text-button
  [m game]
  (when (and (:highlighted? m) (mouse/clicked? (:mouse game)))
    (when-some [ie (:click-event m)]
      [ie])))

(defn filler
  [rect]
  {:type :ui/filler
   :rect rect})

(defmethod draw-widget! :ui/filler
  [m]
  (graphics/draw-tiled! sprite/blank (:rect m)))

(defn filler-border
  [rect]
  (let [[x y w h] rect]
    (panel
      [(filler [x y w 32])
       (filler [x y 32 h])
       (filler [(+ x w -32) y 32 h])
       (filler [x (+ y h -32) w 32])])))