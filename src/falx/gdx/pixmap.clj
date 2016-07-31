(ns falx.gdx.pixmap
  "A pixmap represens an image in native off-heap memory."
  (:require [falx.gdx.impl.io :as io]
            [falx.gdx.impl.dispatch :as dispatch])
  (:refer-clojure :exclude [format])
  (:import (com.badlogic.gdx.graphics Pixmap Pixmap$Format Pixmap$Blending Pixmap$Filter)))

(defn file->pixmap
  "Returns a pixmap that will be loaded from the image file."
  [file]
  (dispatch/dispatch
    (Pixmap. (io/file-handle file))))

(def ^:private formats
  {::format.alpha Pixmap$Format/Alpha
   ::format.intensity Pixmap$Format/Intensity
   ::format.luminance-alpha Pixmap$Format/LuminanceAlpha
   ::format.rgb565 Pixmap$Format/RGB565
   ::format.rgba4444 Pixmap$Format/RGBA4444
   ::format.rgb888 Pixmap$Format/RGB888
   ::format.rgb8888 Pixmap$Format/RGBA8888})

(def ^:private rformats
  (into {} (map (juxt val key)) formats))

(def ^:private blendings
  {::blending.source-over Pixmap$Blending/SourceOver
   ::blending.none Pixmap$Blending/None
   nil Pixmap$Blending/None})

(def ^:private filters
  {::filter.nearest-neighbour Pixmap$Filter/NearestNeighbour
   ::filter.bilnear Pixmap$Filter/BiLinear})

(defn pixmap
  "Returns a new blank pixmap"
  ([size]
   (pixmap size {}))
  ([size opts]
   (let [[w h] size
         {:keys [format]
          :or {format ::format.rgb8888}} opts]
     (dispatch/dispatch
       (Pixmap.
         (int w)
         (int h)
         ^Pixmap$Format (formats format))))))

(defn size
  "Returns the size of the pixmap"
  [pixmap]
  [(.getWidth pixmap) (.getHeight pixmap)])

(defn format
  "Returns the format of the pixmap"
  [pixmap]
  (rformats (.getFormat pixmap)))

(defn info
  "Returns a map of information about the pixmap"
  [pixmap]
  {::format (format pixmap)
   ::size (size pixmap)})

(defn set-blending!
  "Sets the global blending mode"
  [blending]
  (Pixmap/setBlending (blendings blending)))

(defn set-filter!
  "Sets the global filter"
  [filter]
  (Pixmap/setBlending (filters filter)))

(defn set-color!
  "Sets the color to use for further drawing commands"
  [pixmap color]
  (let [[r g b a] color]
    (.setColor pixmap r g b a)))

(defn fill!
  "Fills the pixmap with a color"
  ([pixmap]
   (.fill pixmap))
  ([pixmap color]
   (set-color! pixmap color)
   (fill! pixmap)))

(defn draw-line!
  "Draws a line on the pixmap"
  ([pixmap line]
   (let [[x y x2 y2] line]
     (.drawLine pixmap x y x2 y2)))
  ([pixmap line color]
   (set-color! pixmap color)
   (draw-line! pixmap line color)))

(defn draw-circle!
  "Draws a circle on the pixmap"
  ([pixmap circle]
   (let [[x y r] circle]
     (.drawCircle pixmap x y r)))
  ([pixmap circle color]
   (set-color! pixmap color)
   (draw-circle! pixmap circle )))

(defn fill-circle!
  "Draws a filled circle on the pixmap"
  ([pixmap circle]
   (let [[x y r] circle]
     (.fillCircle pixmap x y r)))
  ([pixmap circle color]
   (set-color! pixmap color)
   (fill-circle! pixmap circle)))

(defn draw-rect!
  "Draws a rectangle on the pixmap"
  ([pixmap rect]
   (let [[x y w h] rect]
     (.drawRectangle pixmap x y w h)))
  ([pixmap rect color]
   (set-color! pixmap color)
   (draw-rect! pixmap rect color)))

(defn fill-rect!
  "Draws a filled rectangle on the pixmap"
  ([pixmap rect]
   (let [[x y w h] rect]
     (.fillRectangle pixmap x y w h)))
  ([pixmap rect color]
   (set-color! pixmap color)
   (fill-rect! pixmap rect)))

(defn draw-triangle!
  "Draws a triangle on the pixmap"
  ([pixmap triangle]
   (let [[x1 y1 x2 y2 x3 y3] triangle]
     (draw-line! pixmap [[x1 y1] [x2 y2]])
     (draw-line! pixmap [[x1 y1] [x3 y3]])
     (draw-line! pixmap [[x2 y2] [x3 y3]])))
  ([pixmap triangle color]
   (set-color! pixmap color)
   (draw-triangle! pixmap triangle)))

(defn fill-triangle!
  "Draws a filled triangle on the pixmap"
  ([pixmap triangle]
   (let [[x1 y1 x2 y2 x3 y3] triangle]
     (.fillTriangle pixmap x1 y1 x2 y2 x3 y3)))
  ([pixmap triangle color]
   (set-color! pixmap color)
   (fill-triangle! pixmap triangle)))

(defn draw-pixel!
  "Draws a single pixel on the pixmap"
  ([pixmap pt]
   (let [[x y] pt]
     (.drawPixel pixmap x y)))
  ([pixmap pt color]
   (set-color! pixmap color)
   (draw-pixel! pixmap pt)))

(defn draw-pixels!
  "Draws the seq of points on the pixmap"
  ([pixmap pts]
   (run! #(draw-pixel! pixmap %) pts))
  ([pixmap color pts]
   (set-color! pixmap color)
   (draw-pixels! pixmap pts)))