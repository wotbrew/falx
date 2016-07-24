(ns falx.draw
  "Drawing functions and drawable things"
  (:require [clj-gdx :as gdx]
            [clojure.java.io :as io]
            [gdx.color :as color]
            [falx.size :as size])
  (:import (clojure.lang Sequential)))

(defn- invoke
  [f]
  (f))

(defprotocol IDrawImmediate
  (-draw! [this x y w h opts]
    "Implementations can optimise a faster path when asked to be drawn right now."))

(defprotocol IDraw
  (-drawfn [this x y w h opts]
    "Returns a 0-arg fn that will draw the drawable to the screen."))

(defn drawfn
  "Returns a 0-arg fn that will draw the drawable to the screen."
  ([drawable rect]
   (let [[x y w h] rect]
     (-drawfn drawable x y w h {})))
  ([drawable x y w h]
   (-drawfn drawable x y w h {}))
  ([drawable x y w h opts]
   (-drawfn drawable x y w h opts)))

(defn draw!
  "Draws the drawable to the screen."
  ([this rect]
   (let [[x y w h] rect]
     (-draw! this x y w h {})))
  ([this x y w h]
   (-draw! this x y w h {}))
  ([this x y w h opts]
   (-draw! this x y w h opts)))

(extend-protocol IDrawImmediate
  nil
  (-draw! [this x y w h opts]
    (gdx/draw-string! "nil" x y w h opts))
  Object
  (-draw! [this x y w h opts]
    ((-drawfn this x y w h opts)))
  falx.draw.IDraw
  (-draw! [this x y w h opts]
    ((-drawfn this x y w h opts)))
  Sequential
  (-draw! [this x y w h opts]
    (run! #(-draw! % x y w h opts) this)))

(extend-protocol IDraw
  nil
  (-drawfn [this x y w h opts]
    (fn []
      (gdx/draw-string! this x y w opts)))
  Object
  (-drawfn [this x y w h opts]
    (fn []
      (gdx/draw-string! this x y w opts)))
  falx.draw.IDrawImmediate
  (-drawfn [this x y w h opts]
    (fn []
      (-draw! this x y w h opts)))
  Sequential
  (-drawfn [this x y w h opts]
    (let [fs (mapv #(-drawfn % x y w h opts) this)]
      (fn [] (run! invoke fs)))))

;; ====
;; Strings
;; ====

(defn- fast-merge
  [m1 m2]
  (if m1
    (if m2
      (conj m1 m2)
      m1)
    m2))

(defrecord Text [s font opts]
  IDraw
  (-drawfn [this x y w h opts2]
    (let [context (fast-merge opts opts2)]
      (if (:centered? context)
        (let [[tw th] (gdx/get-string-wrapped-bounds s w font)
              [x y w] (size/center tw th x y w h)]
          (fn []
            (gdx/draw-string! s x y w context font)))
        (fn []
          (gdx/draw-string! s x y w context font)))) )
  IDrawImmediate
  (-draw! [this x y w h opts2]
    (let [context (fast-merge opts opts2)]
      (if (:centered? context)
        ((-drawfn this x y w h opts2))
        (gdx/draw-string! s x y w context font)))))

(defn text?
  [x]
  (instance? Text x))

(defn text
  ([s]
   (text s gdx/default-font))
  ([s font]
   (text s font {}))
  ([s font opts]
   (if (text? s)
     (assoc s :font font :opts opts)
     (->Text s font opts))))

(defn text-bounds
  [txt]
  (let [txt (text txt)]
    (gdx/get-string-bounds (:s txt) (:font txt))))

;; =====
;; Sprites
;; =====

(defrecord Sprite [texture rect opts flip-y?]
  IDrawImmediate
  (-draw! [this x y w h opts2]
    (let [context (fast-merge opts2 opts)]
      (gdx/draw-sprite! this x y w h context))))

(defn sprite?
  [x]
  (instance? Sprite x))

(defn sprite
  ([spr]
   (map->Sprite spr))
  ([spr opts]
   (map->Sprite (assoc spr :opts opts))))

(defn tile
  [file x y w h]
  (-> (gdx/sprite (io/resource (format "tiles/%s.png" file)) [x y w h])
      sprite))

(defn- tile32
  [file x y]
  (tile file
        (int (* x 32))
        (int (* y 32))
        32
        32))

;; ====
;; Colors
;; ====

(defn- dim
  [color]
  (color/scale color 0.5))

(def color-selected color/green)
(def color-selected2 color/yellow)
(def color-disabled color/gray)

(def color-default (color/scale color/white 0.9))
(def color-highlight color/white)


;; =====
;; Sprites - Misc
;; =====

(defn- misc32
  [x y]
  (tile32 "Misc" x y))

(def selection
  (misc32 0 0))

(def pixel
  (misc32 0 1))

(def pixel-black
  (sprite pixel {:color color/black}))

(def pixel-gray
  (sprite pixel {:color color/gray}))

;; =====
;; Sprites - Races - Human
;; =====

(defn- human32
  [x y]
  (tile32 "Human" x y))

(def human-female
  (human32 0 0))

(def human-male
  (human32 1 0))

;; =====
;; Sprites - Gui
;; =====

(defn- gui32
  [x y]
  (tile32 "Gui" x y))

(def block
  (gui32 0 0))

(def slot
  (gui32 1 0))

;; =====
;; Sprites - Mouse
;; =====

(defn- mouse32
  [x y]
  (tile32 "Mouse" x y))

(def mouse-point
  (mouse32 0 0))

(def mouse-select
  (mouse32 1 0))

(def mouse-attack
  (mouse32 2 0))

(def mouse-gray-attack
  (mouse32 3 0))

(def mouse-spell
  (mouse32 0 1))

(def mouse-gray-spell
  (mouse32 1 1))

(def mouse-left
  (mouse32 2 1))

(def mouse-right
  (mouse32 3 1))

(def mouse-arrow
  (mouse32 0 2))

(def mouse-gray-arrow
  (mouse32 1 2))

;; ====
;; Sprites - Decor
;; ====

(defn- decor32
  [x y]
  (tile32 "Decorations1" x y))

(def torch1
  (decor32 0 0))

(def torch2
  (decor32 1 0))

(def torch3
  (decor32 2 0))

(def torch4
  (decor32 3 0))

;; ====
;; Boxes
;; ====

(defrecord Box [thickness opts]
  IDraw
  (-drawfn [this x y w h opts2]
    (let [opts (fast-merge opts opts2)
          t thickness
          r-t (+ x (- w t))
          b-t (+ y (- h t))]
      (fn []
        (draw! pixel x y w t opts)
        (draw! pixel r-t y 1 h opts)
        (draw! pixel x b-t w 1 opts)
        (draw! pixel x y t h opts))))
  IDrawImmediate
  (-draw! [this x y w h opts2]
    (let [opts (fast-merge opts opts2)
          t thickness
          r-t (+ x (- w t))
          b-t (+ y (- h t))]
      (draw! pixel x y w t opts)
      (draw! pixel r-t y 1 h opts)
      (draw! pixel x b-t w 1 opts)
      (draw! pixel x y t h opts))))

(def default-box
  (->Box 1 {}))

(defn box
  ([]
   default-box)
  ([thickness]
   (->Box thickness {}))
  ([thickness opts]
   (->Box thickness opts)))

(defrecord CustomBox [thickness lopts topts ropts bopts]
  IDraw
  (-drawfn [this x y w h _]
    (let [t thickness
          r-t (+ x (- w t))
          b-t (+ y (- h t))]
      (fn []
        (draw! pixel x y w t topts)
        (draw! pixel r-t y 1 h ropts)
        (draw! pixel x b-t w 1 bopts)
        (draw! pixel x y t h lopts))))
  IDrawImmediate
  (-draw! [this x y w h _]
    (let [t thickness
          r-t (+ x (- w t))
          b-t (+ y (- h t))]
      (draw! pixel x y w t topts)
      (draw! pixel r-t y 1 h ropts)
      (draw! pixel x b-t w 1 bopts)
      (draw! pixel x y t h lopts))))

(defn shaded-box
  [thickness color]
  (->CustomBox
    thickness
    {:color color}
    {:color color}
    {:color (dim color)}
    {:color (dim color)}))

;; ====
;; Button
;; ====

(def button-box-focused
  (shaded-box 1 color-highlight))

(def button-box-selected
  (shaded-box 1 color-selected))

(def button-box-default
  (shaded-box 1 color-default))

(def button-box-disabled
  (shaded-box 1 color-disabled))

(defn button-box
  [state]
  (case state
    ::button-state.focused button-box-focused
    ::button-state.selected button-box-selected
    ::button-state.disabled button-box-disabled
    button-box-default))

(defn button-text-opts
  [state]
  {:centered? true
   :color
   (case state
     ::button-state.focused color-selected
     ::button-state.selected color-highlight
     ::button-state.disabled color-disabled
     color-default)})

(def button-font
  gdx/default-font)

(defn button-text
  [txt state]
  (text txt button-font (button-text-opts state)))

(defn button
  ([txt]
   (button txt ::button-state.default))
  ([txt state]
   [pixel-black
    (button-box state)
    (button-text txt state)]))