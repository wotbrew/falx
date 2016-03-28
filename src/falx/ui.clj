(ns falx.ui
  (:require [gdx.color :as color]
            [falx.game :as g]
            [falx.sprite :as sprite]))

;; Colors

(def gray
  (color/color 0.5 0.5 0.5 1))

(def light-gray
  (color/color 0.75 0.75 0.75 1))

(def white
  (color/color 1 1 1 1))

(def black
  (color/color 0 0 0 1))

(def yellow
  (color/color 1 1 0 1))

(def green
  (color/color 0 1 0 1))

(def red
  (color/color 1 0 0 1))

;; Widgets

(defn sprite
  ([s rect]
   (sprite sprite rect nil))
  ([s rect context]
   {:type   :actor/ui-sprite
    :sprite s
    :rect   rect
    :context context}))

(defn player-index
  ([idx rect]
   {:type :actor/ui-player-index
    :index idx
    :rect rect}))

(defn pixel
  ([rect]
   (pixel rect nil))
  ([rect context]
   (sprite sprite/pixel rect context)))

(defn box
  ([rect]
   (box rect nil))
  ([rect context]
   {:type    :actor/ui-box
    :rect    rect
    :context context}))

(defn flatten-children
  [a]
  (cons (update a :ui-children (partial mapv :id))
        (mapcat #(flatten-children %) (:ui-children a))))