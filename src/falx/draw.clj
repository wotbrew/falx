(ns falx.draw
  "Drawing functions and drawable things"
  (:require [clj-gdx :as gdx]
            [clojure.java.io :as io]
            [gdx.color :as color]
            [falx.size :as size]))

(defprotocol IDrawImmediate
  (-draw! [this x y w h opts]
    "Implementations can optimise a faster path when asked to be drawn right now."))

(defprotocol IDraw
  (-drawfn [this x y w h opts]
    "Returns a 0-arg fn that will draw the drawable to the screen."))

(defprotocol ISized
  (-size [this]))

(defn size
  [drawable]
  (-size drawable))

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
    ((-drawfn this x y w h opts))))

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
      (-draw! this x y w h opts))))

(extend-protocol ISized
  nil
  (-size [this]
    nil)
  Object
  (-size [this]
    (let [w (:w this)
          h (:h this)]
      (when (and w h)
        [w h]))))

;; ====
;; Combinators
;; ====

(defn- invoke
  [f]
  (f))

(defrecord Stack [drawables]
  IDraw
  (-drawfn [this x y w h opts]
    (let [fs (mapv #(-drawfn % x y w h opts) drawables)]
      (fn []
        (run! invoke fs))))
  IDrawImmediate
  (-draw! [this x y w h opts]
    (run! (fn [d] (draw! d x y w h opts)) drawables)))

(defn stack
  "Returns a drawable that will draw the inner drawables back-to-front."
  [drawables]
  (->Stack drawables))

(defrecord At [drawable x y]
  IDraw
  (-drawfn [this x2 y2 w h opts]
    (-drawfn drawable (+ x x2) (+ y y2) w h opts))
  IDrawImmediate
  (-draw! [this x2 y2 w h opts]
    (-draw! drawable (+ x x2) (+ y y2) w h opts)))

(defn at
  "Returns a drawable offset by `x` and `y`."
  ([drawable pt]
   (let [[x y] pt]
     (->At drawable x y)))
  ([drawable x y]
   (->At drawable x y)))

(defrecord Center [drawable w h]
  IDraw
  (-drawfn [this x y w2 h2 opts]
    (let [[x y w h] (size/center w h x y w2 h2)]
      (-drawfn drawable x y w h opts))))

(defn center
  "Centers the drawable according to the given size."
  ([drawable size]
   (let [[w h] size]
     (center drawable w h)))
  ([drawable w h]
   (->Center drawable w h)))

(defrecord Fit [drawable w h]
  IDraw
  (-drawfn [this x y w2 h2 opts]
    (-drawfn drawable x y (min w w2) (min h h2) opts))
  IDrawImmediate
  (-draw! [this x y w2 h2 opts]
    (-draw! drawable x y (min w w2) (min h h2) opts)))

(defn fit
  "Fits the drawable to the size (if the requested size is larger)"
  ([drawable size]
   (let [[w h] size]
     (->Fit drawable w h)))
  ([drawable w h]
   (->Fit drawable w h)))

(defrecord Rows [drawables]
  IDraw
  (-drawfn [this x y w h opts]
    (let [n (count drawables)
          ih (long (/ h n))
          fs (into []
                   (map-indexed (fn [i d]
                                  (-drawfn d x (+ y (* ih i)) w ih opts)))
                   drawables)]
      (fn [] (run! invoke fs)))))

(defn rows
  ([drawables]
   (->Rows drawables)))

(defrecord Cols [drawables]
  IDraw
  (-drawfn [this x y w h opts]
    (let [n (count drawables)
          iw (long (/ w n))
          fs (into []
                   (map-indexed (fn [i d]
                                  (-drawfn d (+ x (* iw i)) y iw h opts)))
                   drawables)]
      (fn [] (run! invoke fs)))))

(defn cols
  ([drawables]
   (->Cols drawables)))

(defrecord Items [w h drawables]
  IDraw
  (-drawfn [this x y w2 h2 opts]
    (let [cols (long (/ w2 w))
          rows (long (/ h2 h))
          fs  (into []
                   (map-indexed (fn [i d]
                                  (let [col (mod i cols)
                                        row (mod (long (/ i cols)) rows)]
                                    (-drawfn d
                                             (+ x (* w col))
                                             (+ y (* h row))
                                             w
                                             h
                                             opts))))
                   drawables)]
      (fn [] (run! invoke fs)))))

(defn items
  ([size drawables]
   (let [[w h] size]
     (items w h drawables)))
  ([w h drawables]
   (->Items w h drawables)))

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
      (fn []
        (gdx/draw-string! s x y w context font))) )
  IDrawImmediate
  (-draw! [this x y w h opts2]
    (let [context (fast-merge opts opts2)]
      (gdx/draw-string! s x y w context font))))

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

(defrecord Sprite [texture rect flip-y?]
  IDrawImmediate
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

(def button-box-default
  (shaded-box 1 color-default))

(def button-box-disabled
  (shaded-box 1 color-disabled))

(defn button-box
  [state]
  (case state
    :falx.ui.button/state.focused button-box-focused
    :falx.ui.button/state.disabled button-box-disabled
    button-box-default))

(defn button-text-opts
  [state]
  {:color
   (case state
     :falx.ui.button/state.focused color-highlight
     :falx.ui.button/state.disabled color-disabled
     color-default)})

(defrecord Button [txt state]
  IDraw
  (-drawfn [this x y w h opts]
    (let [button-box (button-box state)
          text-opts (button-text-opts state)]
      (fn []
        (draw! button-box x y w h)
        (draw! txt x y w h text-opts)))))

(defn button
  ([txt]
   (button txt nil))
  ([txt state]
   (let [txt (text txt)]
     (->Button (center txt (text-bounds txt)) state))))
