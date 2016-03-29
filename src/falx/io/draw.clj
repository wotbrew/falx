(ns falx.io.draw
  "Drawing functions"
  (:require [clj-gdx :as gdx]
            [falx.size :as size]
            [falx.sprite :as sprite]
            [gdx.color :as color]
            [falx.game :as g]
            [clojure.java.io :as io])
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

(defn actor!
  [a x y w h context]
  (when (:selected? a)
    (sprite! sprite/selection x y w h {:color color/green}))
  (sprite! sprite/human-male x y w h))

(defn world!
  [g x y w h]
  (let [x (int x)
        y (int y)
        w (int w)
        h (int h)
        xl (- x 32)
        yl (- y 32)
        xr (+ x w 32)
        yr (+ y h 32)]
    (doseq [a (g/query g :level :testing)
            :let [point (:point a)]
            :when point
            :let [[wx wy] point
                  wxp (* 32 (int wx))
                  wyp (* 32 (int wy))]
            :when (and (<= xl wxp xr)
                       (<= yl wyp yr))]
      (actor! a
              wxp
              wyp
              32
              32
              nil))))

(defmulti ui-element!* (fn [g e x y w h] (:type e)))

(defmethod ui-element!* :default
  [g e x y w h])

(defn ui-element!
  [g e xo yo]
  (when-not (:hide? e)
    (when-some [[x y w h] (:rect e)]
      (let [x2 (+ x xo)
            y2 (+ y yo)]
        (ui-element!* g e x2 y2 w h)
        (run! #(ui-element! g % x2 y2) (keep #(if (map? %)
                                               %
                                               (g/get-actor g %))
                                             (:ui-children e)))))))

(defn ui!
  [g]
  (run! #(ui-element! g % 0 0) (g/query g :ui-root? true)))

(defmethod ui-element!* :actor/ui-sprite
  [g e x y w h]
  (sprite! (:sprite e) x y w h (:context e)))

(defmethod ui-element!* :actor/ui-box
  [g e x y w h]
  (box! x y w h (:context e)))

(defmethod ui-element!* :actor/ui-actor
  [g e x y w h]
  (when-some [a (g/get-actor g (:actor-id e))]
    (actor! a x y w h nil)))

(defmethod ui-element!* :actor/viewport
  [g e x y w h]
  (gdx/using-camera
    (:camera e gdx/default-camera)
    (world! g 0 0 w h)))

(defmulti stat-label!
  (fn [a stat x y w h] stat))

(defmethod stat-label! :default
  [a stat x y w h]
  (string! stat x y w h))

(defmethod ui-element!* :actor/ui-stat-label
  [g {:keys [actor-id stat]} x y w h]
  (when-some [a (g/get-actor g actor-id)]
    (stat-label! a stat x y w h)))