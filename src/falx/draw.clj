(ns falx.draw
  "Drawing functions and drawable things"
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
  ([val rect]
   (let [[x y w h] rect]
     (proto/-draw! val x y w h)))
  ([val x y w h]
   (proto/-draw! val x y w h)))

(defn drawfn
  ([drawable rect]
   (let [[x y w h] rect]
     (proto/-drawfn drawable x y w h)))
  ([drawable x y w h]
   (proto/-drawfn drawable x y w h)))

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

(defn font
  ([x]
   (proto/-font x)))

(defn recolor
  ([x color]
   (proto/-recolor x color)))

(defn str!
  ([s rect]
   (gdx/draw-str! s @default-font rect))
  ([s rect font]
   (gdx/draw-str! s (proto/-font font) rect))
  ([s x y w]
   (gdx/draw-str! s @default-font x y w))
  ([s x y w font]
   (gdx/draw-str! s (proto/-font font) x y w)))

(defn region!
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

(declare ->ColoredText)

(defrecord Text [s font]
  proto/IDraw
  (-draw! [this x y w _]
    (gdx/draw-str! s @font x y w))
  proto/IRecolor
  (-recolor [this color]
    (->ColoredText s font color)))

(defrecord ColoredText [s font color]
  proto/IDraw
  (-draw! [this x y w _]
    (gdx/with-font-color @font color (gdx/draw-str! s @font x y w)))
  proto/IRecolor
  (-recolor [this color2]
    (assoc this :color color2)))

(defrecord FontColored [drawable font color]
  proto/IDraw
  (-draw! [this x y w h]
    (gdx/with-font-color @font color (proto/-draw! drawable x y w h))))

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
         (->CenteredText (str s) font)
         (->Text (str s) font))
       (some? color) (recolor color)))))

(defrecord Colored [drawable color]
  proto/IDraw
  (-draw! [this x y w h]
    (gdx/with-region-color
      color
      (proto/-draw! drawable x y w h)))
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

(defn image
  ([region]
   (->FastImage region))
  ([file rect]
   (->Image (delay (gdx/region file rect))))
  ([file rect color]
   (recolor (image file rect) color)))

(def pixel
  (->Image
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
  ([]
   default-box)
  ([opts]
   (let [thickness (:thickness opts 1)]
     (if-some [color (:color opts)]
       (->Colored (->Box thickness) color)
       (->Box thickness)))))

(defn box!
  ([rect]
   (draw! default-box rect))
  ([rect opts]
   (draw! (box opts) rect))
  ([x y w h]
   (draw! default-box x y w h))
  ([x y w h opts]
   (draw! (box opts) x y w h)))