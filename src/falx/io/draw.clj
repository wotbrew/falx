(ns falx.io.draw
  "Drawing functions"
  (:require [clj-gdx :as gdx]
            [falx.position :as pos]
            [falx.size :as size]
            [falx.sprite :as sprite]
            [gdx.color :as color]
            [falx.game :as g]
            [clojure.java.io :as io]
            [falx.rect :as rect]
            [gdx.camera :as camera])
  (:import (clojure.lang IPersistentMap)))

(def font
  (gdx/font (io/resource "default.fnt")))

(defn sprite!
  "Draws a sprite in the rectangle."
  ([sprite rect]
   (sprite! sprite rect nil))
  ([sprite rect context]
   (let [[x y w h] rect]
     (sprite! sprite x y w h context)))
  ([sprite x y w h]
   (sprite! sprite x y w h nil))
  ([sprite x y w h context]
   (gdx/draw-sprite! sprite x y w h context)))

(defn string!
  "Draws the object as a string in the rectangle."
  ([s rect]
   (string! s rect nil))
  ([s rect context]
   (let [[x y w h] rect]
     (string! s x y w h context)))
  ([s x y w h]
   (string! s x y w h nil))
  ([s x y w h context]
   (gdx/draw-string! s x y w context (or (:font context)
                                         font))))

(defn centered-string!
  "Draws the object as a string, centering the text in the rectangle."
  ([s rect]
   (centered-string! s rect nil))
  ([s rect context]
   (let [[x y w h] rect]
     (centered-string! s x y w h context)))
  ([s x y w h]
   (centered-string! s x y w h nil))
  ([s x y w h context]
   (let [font (:font context gdx/default-font)
         bounds (gdx/get-string-wrapped-bounds s w font)
         [x y w] (size/centered-rect bounds x y w h)]
     (gdx/draw-string! s x y w context (or (:font context)
                                           font)))))

(defmulti map! (fn [m x y w h context] (:type m)))

(defmethod map! :default
  [m x y w h context]
  (string! m x y w h context))

(defprotocol IDrawable
  (-draw! [this x y w h context]
    "Implement me in order for your type to be drawn
    by `object!`"))

(extend-protocol IDrawable
  Object
  (-draw! [this x y w h context]
    (string! this x y w h context))
  nil
  (-draw! [this x y w h context]
    (string! "nil" x y w h context))
  IPersistentMap
  (-draw! [this x y w h context]
    (map! this x y w h context)))

(defn drawable
  "Takes a fn of `thing x y w h context` and makes a drawable
  out of it."
  [f]
  (reify IDrawable
    (-draw! [this x y w h context]
      (f this x y w h context))))

(defn object!
  "Draws the object to the screen via its `IDrawable` impl."
  ([o rect]
   (object! o rect nil))
  ([o rect context]
   (let [[x y w h] rect]
     (object! o x y w h context)))
  ([o x y w h]
   (object! o x y w h nil))
  ([o x y w h context]
   (-draw! o x y w h context)))

(defn- get-draw-fn
  [drawable]
  (fn !
    ([rect]
     (! rect nil))
    ([rect context]
     (let [[x y w h] rect]
       (! x y w h context)))
    ([x y w h]
     (! x y w h nil))
    ([x y w h context]
     (-draw! drawable x y w h context))))

(defmulti widget! (fn [e frame] (:type e)))

(defmethod widget! :default
  [e _]
  (when (:rect e)
    (string! (:type e) (:rect e))))

(def box
  "A box, draw with `object`' or `box!`."
  (drawable
    (fn [_ x y w h context]
      (let [t (int (:thickness context 1))
            -t (- t)]
        (gdx/draw-sprite! sprite/pixel x y w t context)
        (gdx/draw-sprite! sprite/pixel x y t h context)
        (gdx/draw-sprite! sprite/pixel x (+ y h -t) w t context)
        (gdx/draw-sprite! sprite/pixel (+ x w -t) y t h context)))))

(def box!
  "Draws a box."
  (get-draw-fn box))

(defn tiled!
  "Draws the sprite by tiling it in the given rect."
  ([sprite rect]
   (tiled! sprite rect nil))
  ([sprite rect context]
   (let [[x y w h] rect]
     (tiled! sprite x y w h context)))
  ([sprite x y w h]
   (tiled! sprite x y w h nil))
  ([sprite x y w h context]
   (let [x (int x)
         y (int y)
         w (int w)
         h (int h)
         xl (int (+ x w))
         yl (int (+ y h))
         [tw th] (:tile-size context (sprite/get-size sprite))
         tw (int tw)
         th (int th)]
     (loop [x x]
       (when (< x xl)
         (loop [y y]
           (when (< y yl)
             (gdx/draw-sprite! sprite x y tw th context)
             (recur (+ y tw))))
         (recur (+ x th)))))))

(defmulti actor! (fn [g a x y w h] (:type a)))

(defmethod actor! :default
  [g a x y w h])

(defmethod actor! :actor/terrain
  [g a x y w h]
  (string! "_" x y w h))

(defmethod actor! :actor/creature
  [g a x y w h]
  (when (:selected? a)
    (sprite! sprite/selection x y w h {:color color/green}))
  (sprite! sprite/human-male x y w h))

(def layers
  [:floor
   :creature])

(defn world!
  [g x y w h cw ch]
  (let [x (int x)
        y (int y)
        w (int w)
        h (int h)
        cw (int cw)
        ch (int ch)
        xl (- x 32)
        yl (- y 32)
        xr (+ x w 32)
        yr (+ y h 32)
        level (g/get-selected-level g)]
    (doseq [layer layers
            :let [slice (pos/slice layer level)]
            a (g/query g :slice slice)
            :let [point (:point a)]
            :when point
            :let [[wx wy] point
                  wxp (* cw (int wx))
                  wyp (* ch (int wy))]
            :when (and (<= xl wxp xr)
                       (<= yl wyp yr))]
      (actor! g
              a
              wxp
              wyp
              cw
              ch))))

(defmulti ui-element!* (fn [g e x y w h] (:type e)))

(defmethod ui-element!* :default
  [g e x y w h])

(defn ui-element!
  [g e]
  (let [[x y w h] (:rect e)]
    (ui-element!* g e x y w h)))

(defmethod ui-element!* :element/sprite
  [g e x y w h]
  (sprite! (:sprite e) x y w h))

(defmethod ui-element!* :element/backing
  [g e x y w h]
  (sprite! sprite/pixel x y w h {:color color/black}))

(def box-context
  {:color color/light-gray})

(defmethod ui-element!* :element/box
  [g e x y w h]
  (box! x y w h box-context))

(def highlighted-box-context
  {:color color/yellow})

(defmethod ui-element!* :element/highlighted-box
  [g e x y w h]
  (box! x y w h highlighted-box-context))

(defmethod ui-element!* :element/actor
  [g e x y w h]
  (when-some [a (g/get-actor g (:id e))]
    (actor! g a x y w h)))

(defmethod ui-element!* :element/viewport
  [g e x y w h]
  (let [cam (:camera e gdx/default-camera)
        [cx cy] (camera/get-world-point cam x y)
        [cw ch] (g/get-cell-size g)]
    (gdx/using-camera
      cam
      (world! g cx cy w h cw ch))))

(defmethod ui-element!* :element/on-hover
  [g e x y w h]
  (if (g/contains-mouse? g x y w h)
    (ui-element! g (:hovering e))
    (ui-element! g (:not-hovering e))))

(defmethod ui-element!* :element/many
  [g e _ _ _ _]
  (run! #(ui-element! g %) (:coll e)))

(defn ui!
  ([g]
   (run! #(ui! g %) (g/query g :ui-root? true)))
  ([g a]
   (doseq [e (let [els (:elements a)]
               (if (map? els)
                 (vals els)
                 els))]
     (ui-element! g e))
   (doseq [c (:ui-children a)
           :let [child (g/get-actor g c)]
           :when child]
     (ui! g child))))
