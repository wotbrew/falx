(ns falx.graphics
  (:require [clj-gdx :as gdx]
            [falx.sprite :as sprite]
            [falx.theme :as theme]
            [falx.rect :as rect]))

(defn draw!
  ([thing point]
   (gdx/draw! thing point))
  ([thing point context]
   (gdx/draw! thing point context)))

(defn draw-in!
  ([thing rect]
   (gdx/draw-in! thing rect))
  ([thing rect context]
   (gdx/draw-in! thing rect context)))

(defn draw-tiled!
  ([thing rect]
    (draw-tiled! thing rect [32 32]))
  ([thing rect size]
    (draw-tiled! thing rect size {}))
  ([thing rect size context]
   (let [[x y w h] rect
         [w2 h2] size
         i (int (/ w w2))
         j (int (/ h h2))]
     (loop [i_ 0
            j_ 0]
       (if (< i_ i)
         (do
           (gdx/-draw-in! thing
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
     (gdx/using-texture-region-context
       context
       (gdx/-draw-in! s x y w 1 {})
       (gdx/-draw-in! s x y 1 h {})
       (gdx/-draw-in! s (+ x w) y 1 h {})
       (gdx/-draw-in! s x (+ y h) w 1 {})))))

(defn draw-centered-string!
  ([rect text]
    (draw-centered-string! rect text {}))
  ([rect text context]
   (let [s (str text)
         bounds (gdx/get-string-wrapped-bounds s (nth rect 2))
         text-rect (rect/center-rect rect bounds)]
     (draw-in! s text-rect context))))

(defn draw-text-button!
  ([rect text]
   (draw-text-button! rect text {:color theme/light-gray}))
  ([rect text context]
    (draw-text-button! rect text context context))
  ([rect text box-context text-context]
   (draw-box! rect box-context)
   (draw-centered-string! rect text text-context)))

(defn draw-highlighted-text-button!
  [rect text]
  (draw-text-button! rect (str "- " text " -") {:color theme/white}))

(defn draw-selected-text-button!
  [rect text]
  (draw-text-button! rect (str "- " text " -") {:color theme/green}))

(defn draw-disabled-text-button!
  [rect text]
  (draw-text-button! rect text {:color theme/gray}))

(defmethod gdx/draw-map-in! :ui/text-button
  [m x y w h context]
  (let [{:keys [highlighted? disabled? text]} m
        rect [x y w h]]
    (cond
      disabled? (draw-disabled-text-button! rect text)
      highlighted? (draw-highlighted-text-button! rect text)
      :else (draw-text-button! rect text))))
