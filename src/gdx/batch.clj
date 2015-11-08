(ns gdx.batch
  (:import (com.badlogic.gdx.graphics.g2d SpriteBatch TextureRegion BitmapFont)
           (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx.math Matrix4)))

(defmacro using
  [batch & body]
  `(let [b# ~batch]
    (.begin b#)
    (try
      ~@body
      (finally
        (.end b#)))))

(defn draw-texture-region!
  ([^SpriteBatch batch ^TextureRegion gdx-texture-region x y]
    (.draw batch gdx-texture-region (float x) (float y)))
  ([^SpriteBatch batch ^TextureRegion gdx-texture-region x y w h]
    (.draw batch gdx-texture-region (float x) (float y) (float w) (float h))))

(defn draw-string!
  [^SpriteBatch batch ^BitmapFont font s x y]
  (.drawMultiLine font batch (str s) (float x) (float y)))

(defn draw-string-wrapped!
  [^SpriteBatch batch ^BitmapFont font s x y w]
  (.drawWrapped font batch (str s) (float x) (float y) (float w)))

(defn set-color-float-bits!
  [^SpriteBatch batch float-bits]
  (.setColor batch (float float-bits)))

(defn get-color-float-bits
  [^SpriteBatch batch]
  (let [^Color color (.getColor batch)]
    (.toFloatBits color)))

(defmacro using-color-float-bits
  [batch float-bits & body]
  `(let [b# ~batch
         old-color# (get-color-float-bits b#)]
     (set-color-float-bits! b# ~float-bits)
     (let [r# (do ~@body)]
       (set-color-float-bits! b# old-color#)
       r#)))

(defmacro using-projection-matrix
  [batch matrix & body]
  `(let [^SpriteBatch b# ~batch
         ^Matrix4 m# ~matrix
         ^Matrix4 o# (.getProjectionMatrix b#)]
     (.setProjectionMatrix b# m#)
     (let [r# (do ~@body)]
       (.setProjectionMatrix b# o#)
       r#)))