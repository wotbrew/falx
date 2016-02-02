(ns falx.ui.widget
  (:require [falx.rect :as rect])
  (:refer-clojure :exclude [update]))

(defn text
  [s rect]
  {:type :ui.type/text
   :text s
   :rect rect})

(defn box
  [rect]
  {:type :ui.type/box
   :rect rect})

(defn panel
  [rect coll]
  {:type :ui.type/panel
   :rect rect
   :elements coll})

(derive :ui.type/screen :ui.type/panel)

(defmulti handle-frame (fn [ui widget frame] (:type widget)))

(defmethod handle-frame :default
  [ui widget frame]
  ui)