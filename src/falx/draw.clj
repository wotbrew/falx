(ns falx.draw
  "Functions to enable drawing things to the screen"
  (:require [falx.gdx :as gdx]
            [falx.gdx.pixmap :as pixmap]
            [falx.draw.protocols :as proto]
            [falx.gdx.texture :as texture]
            [falx.size :as size])
  (:import (com.badlogic.gdx.graphics.g2d BitmapFont TextureRegion)
           (java.io File)
           (clojure.lang IDeref)
           (falx.draw.protocols IRegionColored)))

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

(extend-protocol proto/IDrawLater
  nil
  (-drawfn [this x y w h]
    (fn []
      (draw! nil x y w h)))
  Object
  nil
  (-drawfn [this x y w h]
    (fn []
      (draw! nil x y w h))))

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
  ([x color]
   (proto/-recolor x color)))

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
  TextureRegion
  (-draw! [this x y w h]
    (region! this x y w h)))

(defrecord Text [s font]
  proto/IDraw
  (-draw! [this x y w _]
    (gdx/draw-str! s font x y w)))

(declare ->FontColored)

(defrecord Text [s font]
  proto/IDraw
  (-draw! [this x y w _]
    (gdx/draw-str! s @font x y w))
  proto/IRecolor
  (-recolor [this color]
    (->FontColored s font color)))

(defrecord FontColored [d font color]
  proto/IDraw
  (-draw! [this x y w h]
    (gdx/with-font-color @font color (proto/-draw! d x y w h))))

(defrecord CenteredText [s font]
  proto/IDraw
  (-draw! [this x y w h]
    ((proto/-drawfn this x y w h) nil))
  proto/IDrawLater
  (-drawfn [this x y w h]
    (let [bounds (gdx/str-bounds s @font)
          center (size/center bounds x y w h)]
      (fn []
        (gdx/draw-str! s @font center))))
  proto/IRecolor
  (-recolor [this color]
    (->FontColored this font color)))

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
       (if (:centered? opts)
         (->CenteredText s font)
         (->Text s font))
       (some? color) (recolor color)))))

(defrecord Colored [d color]
  proto/IDraw
  (-draw! [this x y w h]
    (gdx/with-region-color
      color
      (proto/-draw! d x y w h)))
  proto/IRecolor
  (-recolor [this color2]
    (assoc this :color color2)))

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
  proto/IDraw
  (-draw! [this x y w h]
    (region! region x y w h))
  proto/IRecolor
  (-recolor [this color]
    (->Colored this color)))

(defrecord Image [region]
  proto/IDraw
  (-draw! [this x y w h]
    (region! @region x y w h))
  proto/IRecolor
  (-recolor [this color]
    (->Colored this color)))

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

(defn box
  "Returns a box drawable
  opts
   `:thickness` the thickness of the box lines.
   `:color` an rgba color vector."
  ([]
   default-box)
  ([opts]
   (let [thickness (:thickness opts 1)]
     (if-some [color (:color opts)]
       (->Colored (->Box thickness) color)
       (->Box thickness)))))