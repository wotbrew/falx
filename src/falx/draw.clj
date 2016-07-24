(ns falx.draw
  (:require [clj-gdx :as gdx]
            [clojure.java.io :as io]
            [gdx.color :as color]))

(defprotocol IDraw
  (-draw! [this x y w h opts]))

(defn draw!
  ([this rect]
   (let [[x y w h] rect]
     (-draw! this x y w h {})))
  ([this x y w h]
   (-draw! this x y w h {}))
  ([this x y w h opts]
   (-draw! this x y w h opts)))

(extend-protocol IDraw
  nil
  (-draw! [this x y w h opts]
    (gdx/draw-string! "nil" x y w h opts))
  Object
  (-draw! [this x y w h opts]
    (gdx/draw-string! this x y w opts)))

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
  (-draw! [this x y w h opts2]
    (let [context (fast-merge opts opts2)]
      (gdx/draw-string! s x y w context font))))

(defn text
  ([s]
   (->Text s gdx/default-font {}))
  ([s font]
   (->Text s font {}))
  ([s font opts]
   (->Text s font opts)))

(defn text-padding
  [text w h]
  (let [[w1 h1] (gdx/get-string-bounds (:s text) (:font text))]
    [(float (/ (- w w1) 2))
     (float (/ (- h h1) 2))]))

(defn text?
  [x]
  (instance? Text x))

;; =====
;; Sprites
;; =====

(defrecord Sprite [texture rect flip-y?]
  IDraw
  (-draw! [this x y w h opts]
    (gdx/draw-sprite! this x y w h opts)))

(defn sprite
  [file x y w h]
  (map->Sprite (gdx/sprite file [x y w h])))

(defn tile
  [file x y w h]
  (sprite (io/resource (format "tiles/%s.png" file)) x y w h))

(defn- tile32
  [file x y]
  (tile file
        (int (* x 32))
        (int (* y 32))
        32
        32))

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
;; Boxes
;; ====

(defrecord Box [thickness opts]
  IDraw
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

;; ====
;; Colors
;; ====

(def color-selected color/green)
(def color-selected2 color/yellow)
(def color-disabled color/gray)

(def color-default (color/scale color/white 0.95))
(def color-highlight color/white)

;; ====
;; Button
;; ====

(defn button-box-opts
  [state]
  {:color
   (case state
     :ui.button/state.focused color-highlight
     :ui.button/state.disabled color-disabled
     color-default)})

(defn button-text-opts
  [state]
  {:color
   (case state
     :ui.button/state.focused color-highlight
     :ui.button/state.disabled color-disabled
     color-default)})

(defrecord Button [text state]
  IDraw
  (-draw! [this x y w h opts]
    (let [[xp yp] (text-padding text w h)]
      (draw! default-box x y w h (button-box-opts state))
      (draw! text (+ x xp) (+ y yp) w h (button-text-opts state)))))

(defn button
  ([text]
   (button text nil))
  ([text state]
   (->Button (if (text? text) text (falx.draw/text text)) state)))