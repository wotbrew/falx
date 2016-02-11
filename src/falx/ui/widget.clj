(ns falx.ui.widget
  (:require [falx.rect :as rect]
            [falx.ui :as ui])
  (:refer-clojure :exclude [update]))

(defn as
  [id widget]
  (assoc widget :id id))

(defn panel
  ([coll]
   (panel [0 0 0 0] coll))
  ([rect coll]
   {:type     :ui.type/panel
    :rect     rect
    :elements coll}))

(defn update-ui-elements
  [ui widget frame prect]
  (let [rect (:rect widget)
        rs (rect/shift rect prect)]
    (reduce #(ui/update-ui %1 %2 frame rs) ui (:elements widget))))

(defmethod ui/update-ui :ui.type/panel
  [ui widget frame prect]
  (update-ui-elements ui widget frame prect))

(derive :ui.type/clickable :ui.type/panel)

(defn clickable
  [widget]
  {:type     :ui.type/clickable
   :rect     [0 0 0 0]
   :elements [widget]})

(defmulti clicked (fn [ui widget frame] (:id widget)))

(defmethod clicked :default
  [ui _ _]
  ui)

(defn clicked?
  [widget frame prect]
  (let [input (:input frame)
        mouse (:mouse input)]
    (and (contains? (:hit mouse) :left)
         (rect/contains-point? (rect/shift (:rect widget) prect) (:point mouse)))))

(defmethod ui/update-ui :ui.type/clickable
  [ui widget frame prect]
  (let [w (first (:elements widget))]
    (cond-> ui
            (clicked? w frame prect) (clicked w frame)
            :always (update-ui-elements widget frame prect))))


(derive :ui.type/screen :ui.type/panel)

(defn screen
  ([coll]
   {:type :ui.type/screen
    :rect [0 0 0 0]
    :elements coll}))

(defmulti update-ui-screen (fn [ui widget frame] (:id widget)))

(defmethod update-ui-screen :default
  [ui _ _]
  ui)

(defmethod ui/update-ui :ui.type/screen
  [ui widget frame prect]
  (-> (update-ui-screen ui widget frame)
      (update-ui-elements widget frame prect)))

(defn text
  ([s]
   (text s [0 0 1024 0]))
  ([s rect]
   {:type :ui.type/text
    :text s
    :rect rect}))

(defn ctext
  ([s rect]
    {:type :ui.type/centered-text
     :text s
     :rect rect}))

(defn box
  [rect]
  {:type :ui.type/box
   :rect rect})

(defn backing
  [rect]
  {:type :ui.type/backing
   :rect rect})

(defn block
  [rect]
  {:type :ui.type/block
   :rect rect})

(defn arrow-left
  [rect]
  (ctext "<" rect))

(defn arrow-right
  [rect]
  (ctext ">" rect))

(defn block-box
  [rect]
  (let [[x y w h] rect]
    (panel
      rect
      [(block [0 0 w 32])
       (block [0 (+ h -32) w 32])
       (block [(+ w -32) 0 32 h])
       (block [0 0 32 h])])))

(defn block-box-inner-rect
  [block-box]
  (let [[x y w h] (:rect block-box)]
    [(+ x 32) (+ y 32) (- w 64) (- h 64)]))

(defn fps
  ([frame]
   (fps frame [0 0 64 32]))
  ([frame rect]
   (text (:fps frame) rect)))
