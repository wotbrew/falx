(ns falx.graphics.image
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [falx.application :as app])
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

(defn get-texture-region
  [texture x y w h]
  (doto (TextureRegion. ^Texture texture (int x) (int y) (int w) (int h))
    (.flip false true)))

(def sprite-edn
  (-> (io/resource "sprites.edn")
      slurp
      edn/read-string))

(defn find-sprite
  [key]
  (app/on-render-thread
    (let [[file [x y w h]] (get sprite-edn key)
          texture (get-cached-texture (io/resource (format "tiles/%s.png" file)))]
      (get-texture-region texture x y w h))))

(def sprite-cache (atom {}))

(defn find-cached-sprite
  [key]
  (if-some [v (get @sprite-cache key)]
    v
    (get (swap! sprite-cache assoc key (find-sprite key)) key)))

(defn draw!
  ([key point-or-rectangle]
   (if (= (count point-or-rectangle) 4)
     (let [[x y w h] point-or-rectangle]
       (draw! key x y w h))
     (draw! key (nth point-or-rectangle 0) (nth point-or-rectangle 1))))
  ([key x y]
   (let [^TextureRegion sprite (find-cached-sprite key)
         ^SpriteBatch batch @app/sprite-batch]
     (.draw batch sprite (float x) (float y))))
  ([key x y w h]
   (let [^TextureRegion sprite (find-cached-sprite key)
         ^SpriteBatch batch @app/sprite-batch]
     (.draw batch sprite (float x) (float y) (float w) (float h)))))

(defmacro with-color
  [color & forms]
  `(let [^SpriteBatch batch# @app/sprite-batch
         o# (.getColor batch#)
         ^Color n# ~color]
     (.setColor batch# n#)
     ~@forms
     (.setColor batch# o#)))


