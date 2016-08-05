(ns falx.gdx.texture
  (:require [falx.gdx.impl.dispatch :as dispatch]
            [falx.gdx.impl.io :as io])
  (:import (com.badlogic.gdx.graphics Texture Pixmap Pixmap$Format)
           (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics.g2d TextureRegion)))

(def ^:private formats
  {::format.alpha Pixmap$Format/Alpha
   ::format.intensity Pixmap$Format/Intensity
   ::format.luminance-alpha Pixmap$Format/LuminanceAlpha
   ::format.rgb565 Pixmap$Format/RGB565
   ::format.rgba4444 Pixmap$Format/RGBA4444
   ::format.rgb888 Pixmap$Format/RGB888
   ::format.rgb8888 Pixmap$Format/RGBA8888})

(defn texture
  ([file]
   (dispatch/dispatch
     (Texture. (io/file-handle file))))
  ([file opts]
   (dispatch/dispatch
     (Texture.
       ^FileHandle file
       ^Pixmap$Format (formats (:format opts))
       (boolean (:mipmap? opts))))))

(defn pixmap->texture
  ([pixmap]
   (dispatch/dispatch
     (Texture. ^Pixmap pixmap)))
  ([pixmap opts]
   (dispatch/dispatch
     (Texture.
       ^Pixmap pixmap
       ^Pixmap$Format (formats (:format opts))
       (boolean (:mipmap? opts))))))

(defn size
  [texture]
  [(.getWidth texture) (.getHeight texture)])

(defn region
  ([texture rect]
   (let [[x y w h] rect]
     (region texture x y w h)))
  ([texture x y w h]
   (dispatch/dispatch
     (doto
       (TextureRegion.
         ^Texture texture
         (int x)
         (int y)
         (int w)
         (int h))
       (.flip false true)))))