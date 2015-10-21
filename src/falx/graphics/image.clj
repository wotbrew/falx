(ns falx.graphics.image
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [falx.application :as app]
            [falx.rect :as rect]
            [falx.graphics.color :as color])
  (:import (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics Texture Color)
           (com.badlogic.gdx.graphics.g2d TextureRegion SpriteBatch)))

(defn load-texture!
  [file]
  (-> (io/as-file file)
      (FileHandle.)
      (Texture.)))

(defonce get-cached-texture
  (memoize (comp load-texture! io/as-file)))

(defn texture-region
  [texture x y w h]
  (doto (TextureRegion. ^Texture texture (int x) (int y) (int w) (int h))
    (.flip false true)))

(def sprite-edn
  (-> (io/resource "sprites.edn")
      slurp
      edn/read-string))

(defn get-file
  [sprite]
  (let [[file] (get sprite-edn sprite)]
    file))

(defn get-texture-rect
  [sprite]
  (let [[_ rect] (get sprite-edn sprite)]
    rect))

(def get-width
  (comp rect/get-width get-texture-rect))

(def get-height
  (comp rect/get-height get-texture-rect))

(def get-size
  (comp rect/get-size get-texture-rect))

(defn find-texture-region
  [sprite]
  (app/on-render-thread
    (let [file (get-file sprite)
          [x y w h] (get-texture-rect sprite)
          texture (get-cached-texture (io/resource (format "tiles/%s.png" file)))]
      (texture-region texture x y w h))))

(def region-cache (atom {}))

(defn find-cached-texture-region
  [sprite]
  (if-some [v (get @region-cache sprite)]
    v
    (get (swap! region-cache assoc sprite (find-texture-region sprite)) sprite)))

(def get-texture-region
  find-cached-texture-region)

(defn draw!
  ([sprite point-or-rectangle]
   (if (= (count point-or-rectangle) 4)
     (let [[x y w h] point-or-rectangle]
       (draw! sprite x y w h))
     (draw! sprite (nth point-or-rectangle 0) (nth point-or-rectangle 1))))
  ([sprite x y]
   (let [^TextureRegion region (get-texture-region sprite)
         ^SpriteBatch batch @app/sprite-batch]
     (.draw batch region (float x) (float y))))
  ([sprite x y w h]
   (let [^TextureRegion region (get-texture-region sprite)
         ^SpriteBatch batch @app/sprite-batch]
     (.draw batch region (float x) (float y) (float w) (float h)))))

(defmacro with-color
  [color & forms]
  `(let [^SpriteBatch batch# @app/sprite-batch
         o# (.getColor batch#)
         ^Color n# (color/find-color ~color)]
     (.setColor batch# n#)
     ~@forms
     (.setColor batch# o#)))