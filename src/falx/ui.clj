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

(defn actor
  ([id rect]
   {:type :actor/ui-actor
    :id id
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

(defn get-all-actor-ids
  [g]
  (let [f (fn ! [a]
            (let [rst (mapcat ! (:ui-children a))]
              (if-some [id (:id a (when-not (map? a) a))]
                (cons id rst)
                rst)))]
    (mapcat f (g/query g :ui-root? true))))

(defn remove-ui
  [g]
  (reduce g/rem-actor g (get-all-actor-ids g)))