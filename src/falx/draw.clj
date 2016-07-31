(ns falx.draw
  "Functions to enable drawing things to the screen"
  (:require [falx.draw.protocols :as proto]
            [falx.gdx :as gdx]
            [falx.gdx.pixmap :as pixmap]
            [falx.gdx.texture :as texture]
            [falx.size :as size])
  (:import (com.badlogic.gdx.graphics.g2d BitmapFont TextureRegion)
           (java.io File)
           (clojure.lang IDeref)
           (falx.draw.protocols IRegionColored IWrap IImage IDraw)))

(defn draw!
  "Draws the drawable `d` into the given screen rect."
  ([d rect]
   (let [[x y w h] rect]
     (proto/-draw! d x y w h)))
  ([d x y w h]
   (proto/-draw! d x y w h)))

(defn drawfn
  "Draws the returns a 0-ary fn that draws `d` to the screen rect."
  ([d rect]
   (let [[x y w h] rect]
     (proto/-drawfn d x y w h)))
  ([d x y w h]
   (proto/-drawfn d x y w h)))

(defn size
  "Returns the size of the drawable if drawn into the given outer size.
  This is because some drawables may wrap themselves, or fit themselves to the requested size"
  ([d size]
   (let [[w h] size]
     (proto/-size d w h)))
  ([d w h]
   (proto/-size d w h)))

(extend-protocol proto/IDrawLater
  nil
  (-drawfn [this x y w h]
    (fn []
      (draw! nil x y w h)))
  Object
  (-drawfn [this x y w h]
    (fn []
      (draw! this x y w h))))

(def default-font
  (delay
    (gdx/bitmap-font)))

(extend-protocol proto/IFont
  BitmapFont
  (-font [this] this)
  File
  (-font [this]
    (gdx/bitmap-font this)))

(defn recolor
  "Recolors the drawable with the given color"
  ([d color]
   (proto/-recolor d color)))

(defn str!
  "Draws the string into the screen rect using the default font (unless supplied)."
  ([s rect]
   (gdx/draw-str! s @default-font rect))
  ([s rect font]
   (gdx/draw-str! s (proto/-font font) rect))
  ([s x y w]
   (gdx/draw-str! s @default-font x y w))
  ([s x y w font]
   (gdx/draw-str! s (proto/-font font) x y w)))

(defn region!
  "Draws the texture region into the screen rect."
  ([region rect]
   (gdx/draw-region! region rect))
  ([region x y w h]
   (gdx/draw-region! region x y w h)))

(extend-protocol proto/IDraw
  nil
  (-draw! [this x y w h]
    (str! "nil" x y w))
  Object
  (-draw! [this x y w h]
    (str! this x y w))
  IDeref
  (-draw! [this x y w h]
    (proto/-draw! @this x y w h))
  TextureRegion
  (-draw! [this x y w h]
    (region! this x y w h)))

(extend-protocol proto/ISized
  nil
  (-size [this w h]
    (proto/-size "nil" w h))
  Object
  (-size [this w h]
    (proto/-size (str this) w h))
  String
  (-size [this w h]
    (gdx/str-bounds* this @default-font w))
  IWrap
  (-size [this w h]
    (proto/-size (proto/-child this) w h))
  IImage
  (-size [this w h]
    [w h]))

(defrecord Centered [d]
  proto/IDraw
  (-draw! [this x y w h]
    ((proto/-drawfn this x y w h)))
  proto/IDrawLater
  (-drawfn [this x y w h]
    (let [size (proto/-size d w h)
          rect (size/center size x y w h)]
      (drawfn d rect)))
  proto/IRecolor
  (-recolor [this color]
    (update this :d proto/-recolor color))
  proto/IWrap
  (-child [this]
    d))

(defn center
  "Returns a drawable that will center its child within the rect drawn to."
  ([d]
   (->Centered d)))

(declare ->FontColored)

(defrecord Text [s font]
  proto/IDraw
  (-draw! [this x y w _]
    (gdx/draw-str! s @font x y w))
  proto/IRecolor
  (-recolor [this color]
    (->FontColored s font color))
  proto/ISized
  (-size [this w h]
    (gdx/str-bounds* s @font w)))

(defrecord FontColored [d font color]
  proto/IDraw
  (-draw! [this x y w h]
    (gdx/with-font-color @font color (proto/-draw! d x y w h)))
  proto/IWrap
  (-child [this]
    d))

(defn text
  "Creates a text drawable.
  opts:
   `:color` an rgba color vector
   `:font` a java.io.File (.fnt) or BitmapFont to use. (can supply a delay if necessary).
   `:centered?` whether or not to draw the string centered within the target rectangle."
  ([s]
   (->Text (str s) default-font))
  ([s opts]
   (let [font (:font opts default-font)
         font (if (instance? IDeref font)
                font
                (delay (proto/-font font)))
         color (:color opts)]
     (cond->
       (->Text (str s) font)
       (:centered? opts) (center)
       (some? color) (recolor color)))))

(defrecord Colored [d color]
  proto/IDraw
  (-draw! [this x y w h]
    (gdx/with-region-color
      color
      (proto/-draw! d x y w h)))
  proto/IRecolor
  (-recolor [this color2]
    (assoc this :color color2))
  proto/IWrap
  (-child [this]
    d))

(extend-protocol proto/IRecolor
  nil
  (-recolor [this color]
    (text "nil" {:color color}))
  Object
  (-recolor [this color]
    (text this {:color color}))
  TextureRegion
  (-recolor [this color]
    (->Colored this color))
  IRegionColored
  (-recolor [this color]
    (->Colored this color)))

(defrecord FastImage [region]
  proto/IImage
  proto/IRegionColored
  proto/IDraw
  (-draw! [this x y w h]
    (region! region x y w h)))

(defrecord Image [region]
  proto/IImage
  proto/IRegionColored
  proto/IDraw
  (-draw! [this x y w h]
    (region! @region x y w h)))

(defn region->img
  "Returns an image for the given TextureRegion. Can supply an IDeref."
  [region]
  (if (instance? IDeref region)
    (->Image region)
    (->FastImage region)))

(defn- region
  ([file]
   (if (gdx/started?)
     (let [t (gdx/texture file)
           [w h] (texture/size t)]
       (texture/region t 0 0 w h))
     (delay
       (let [t (gdx/texture file)
             [w h] (texture/size t)]
         (texture/region t 0 0 w h)))))
  ([file rect]
   (if (gdx/started?)
     (gdx/region file rect)
     (delay
       (gdx/region file rect)))))

(defn img
  "Returns an image drawable. From the given file.
  opts
   `:rect` a subrectangle of the orignal texture file.
   `:color` an rgba color vector. "
  ([file]
   (region->img (region file)))
  ([file opts]
   (cond->
     (if-some [rect (:rect opts)]
       (region->img (region file rect))
       (img file))
     (:color opts) (recolor (:color opts)))))

(def pixel
  "A 1x1 white image."
  (region->img
    (delay
      (-> (doto (pixmap/pixmap [1 1])
            (pixmap/fill! [1 1 1 1]))
          (texture/pixmap->texture)
          (texture/region [0 0 1 1])))))

(defrecord Box [thickness]
  proto/IImage
  proto/IRegionColored
  proto/IDraw
  (-draw! [this x y w h]
    ((proto/-drawfn this x y w h)))
  proto/IDrawLater
  (-drawfn [this x y w h]
    (let [-t (- thickness)
          x+w (+ x w -t)
          y+h (+ y h -t)]
      (fn []
        (draw! pixel x y w thickness)
        (draw! pixel x+w y thickness h)
        (draw! pixel x y+h w thickness)
        (draw! pixel x y thickness h)))))

(def default-box
  (->Box 1))

(defrecord ShadedBox [thickness color]
  proto/IImage
  proto/IRegionColored
  proto/IDraw
  (-draw! [this x y w h]
    ((proto/-drawfn this x y w h)))
  proto/IDrawLater
  (-drawfn [this x y w h]
    (let [-t (- thickness)
          x+w (+ x w -t)
          y+h (+ y h -t)
          pixel (recolor pixel color)
          [r g b] color
          spixel (recolor pixel [(* 0.3 r) (* 0.3 g) (* 0.3 b) 1])]
      (fn []
        (draw! pixel x y w thickness)
        (draw! pixel x+w y thickness h)
        (draw! spixel x y+h w thickness)
        (draw! spixel x y thickness h)))))

(defn box
  "Returns a box drawable
  opts
   `:thickness` the thickness of the box lines.
   `:color` an rgba color vector.
   `:shaded?` whether or not the box ought to be shaded"
  ([]
   default-box)
  ([opts]
   (let [thickness (:thickness opts 1)]
     (if (:shaded? opts)
       (->ShadedBox thickness (:color opts [1 1 1 1]))
       (if-some [color (:color opts)]
         (->Colored (->Box thickness) color)
         (->Box thickness))))))

(defn- invoke!
  [f]
  (f))

(defrecord Coll [ds]
  proto/IDraw
  (-draw! [this x y w h]
    (run! #(proto/-draw! % x y w h) ds))
  proto/IDrawLater
  (-drawfn [this x y w h]
    (let [fs (mapv #(proto/-drawfn % x y w h) ds)]
      (fn []
        (run! invoke! fs))))
  proto/ISized
  (-size [this w h]
    (let [id [-1 -1]
          maxw (reduce size/maxw id ds)
          maxh (reduce size/maxh id ds)]
      [(if (pos? maxw) maxw w)
       (if (pos? maxh) maxh h)]))
  proto/IRecolor
  (-recolor [this color]
    (->Coll (mapv #(proto/-recolor % color) ds))))

(defn coll
  "Returns a drawable that will draw each drawable in sequential order."
  [ds]
  (->Coll ds))

(defn coll!
  "Immediately draws the given collection of drawables to the screen rect in sequential order."
  ([ds rect]
   (draw! (->Coll ds) rect))
  ([ds x y w h]
   (draw! (->Coll ds) x y w h)))

(defn each
  "Returns a drawable the will draw each drawable in sequential order."
  ([d1]
   d1)
  ([d1 d2]
   (coll [d1 d2]))
  ([d1 d2 d3]
   (coll [d1 d2 d3]))
  ([d1 d2 d3 d4]
   (coll [d1 d2 d3 d4]))
  ([d1 d2 d3 d4 d5]
   (coll [d1 d2 d3 d4 d5]))
  ([d1 d2 d3 d4 d5 & ds]
   (coll (into [d1 d2 d3 d4 d5] ds))))

(defmacro each!
  "Draws the drawables each with the given rect.
  More efficient than `coll!` if you know the rectangle/drawables ahead of time."
  [rect & ds]
  (let [[sx sy sw sh] (repeatedly gensym)
        binding-form (if (vector? rect)
                       (let [[x y w h] rect]
                         `[~sx ~x
                           ~sy ~y
                           ~sw ~w
                           ~sh ~h])
                       `[[~sx ~sy ~sw ~sh] ~rect])]
    `(let ~binding-form
       ~@(for [d ds]
           `(draw! ~d ~sx ~sy ~sw ~sh)))))

(defn in-box
  ([d]
   (in-box d {}))
  ([d opts]
   (each
     (box opts)
     d)))

(defn button
  ([s]
   (button s nil))
  ([s opts]
   (if (:focused? opts)
     (each
       (recolor pixel [0 0 0 1])
       (box {:color [0 1 0 1]
             :shaded? true
             :thickness 2})
       (text (str "- " s " -") {:centered? true
                                :color [0 1 0 1]}))
     (each
       (recolor pixel [0 0 0 1])
       (box {:color [0.7 0.7 0.7 0.7]
             :shaded? true
             :thickness 2})
       (text s {:centered? true
                :color [1 1 1 1]})))))