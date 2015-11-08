(ns falx.graphics
  (:require [clj-gdx :as gdx]
            [falx.sprite :as sprite]
            [falx.theme :as theme]
            [falx.rect :as rect]))

(def draw-sprite! gdx/draw-sprite!)
(def draw-string! gdx/draw-string!)

(defn draw-tiled!
  ([sprite rect]
   (draw-tiled! sprite rect [32 32]))
  ([sprite rect size]
   (draw-tiled! sprite rect size {}))
  ([sprite rect size context]
   (let [[x y w h] rect
         [w2 h2] size
         i (int (/ w w2))
         j (int (/ h h2))]
     (loop [i_ 0
            j_ 0]
       (if (< i_ i)
         (do
           (draw-sprite! sprite
                         (+ x (* i_ w2))
                         (+ y (* j_ h2))
                         w2 h2 context)
           (recur (inc i_) j_))
         (when (< j_ (dec j))
           (recur 0 (inc j_))))))))

(defn draw-box!
  ([rect]
   (draw-box! rect {}))
  ([rect context]
   (let [[x y w h] rect
         s sprite/pixel]
     (gdx/using-sprite-options
       context
       (draw-sprite! s x y w 1)
       (draw-sprite! s x y 1 h)
       (draw-sprite! s (+ x w) y 1 h)
       (draw-sprite! s x (+ y h) w 1)))))

(defn draw-string-centered!
  ([rect text]
   (draw-string-centered! rect text {}))
  ([rect text context]
   (let [s (str text)
         bounds (gdx/get-string-wrapped-bounds s (nth rect 2))
         text-rect (rect/center-rect rect bounds)]
     (draw-string! s text-rect context))))

(defn draw-text-button!
  ([rect text]
   (draw-text-button! rect text {:color theme/light-gray}))
  ([rect text context]
   (draw-text-button! rect text context context))
  ([rect text box-context text-context]
   (draw-box! rect box-context)
   (draw-string-centered! rect text text-context)))

(defn draw-highlighted-text-button!
  [rect text]
  (draw-text-button! rect (str "- " text " -") {:color theme/white}))

(defn draw-selected-text-button!
  [rect text]
  (draw-text-button! rect (str "- " text " -") {:color theme/green}))

(defn draw-disabled-text-button!
  [rect text]
  (draw-text-button! rect text {:color theme/gray}))
