(ns falx.gdx.batch
  (:import (com.badlogic.gdx.graphics.g2d SpriteBatch TextureRegion BitmapFont)
           (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx.math Matrix4)))

(defmacro with
  [batch & body]
  `(let [^SpriteBatch b# ~batch]
     (.begin b#)
     (try
       ~@body
       (finally
         (.end b#)))))

(defn draw-region!
  ([batch region rect]
   (let [[x y w h] rect]
     (draw-region! batch region x y w h)))
  ([batch region x y w h]
   (.draw ^SpriteBatch batch ^TextureRegion region (float x) (float y) (float w) (float h))))

(defn draw-region-at!
  ([batch region pt]
   (let [[x y] pt]
     (draw-region-at! batch region x y)))
  ([batch region x y]
   (.draw ^SpriteBatch batch ^TextureRegion region (float x) (float y))))

(defn draw-str!
  ([batch font s rect]
   (let [[x y w] rect]
     (draw-str! batch font s x y w)))
  ([batch font s x y w]
   (.drawWrapped ^BitmapFont font ^SpriteBatch batch (str s) (float x) (float y) (float w))))

(defn draw-str-at!
  ([batch font s pt]
    (let [[x y] pt]
      (draw-str-at! batch font s x y)))
  ([batch font s x y]
   (.drawMultiLine ^BitmapFont font ^SpriteBatch batch (str s) (float x) (float y))))

(defn set-color!
  [^SpriteBatch batch color]
  (let [[r g b a] color]
    (.setColor batch r g b a)))

(defn color
  [^SpriteBatch batch]
  (let [^Color color (.getColor batch)]
    [(.-r color)
     (.-g color)
     (.-b color)
     (.-a color)]))

(defmacro with-color
  [batch color & body]
  `(let [b# ~batch
         old-color# (falx.gdx.batch/color b#)]
     (set-color! b# ~color)
     (let [r# (do ~@body)]
       (set-color! b# old-color#)
       r#)))

(defmacro with-projection
  [batch matrix & body]
  `(let [^SpriteBatch b# ~batch
         ^Matrix4 m# ~matrix
         ^Matrix4 o# (.cpy (.getProjectionMatrix b#))]
     (.setProjectionMatrix b# m#)
     (let [r# (do ~@body)]
       (.setProjectionMatrix b# o#)
       r#)))