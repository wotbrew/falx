(ns gdx.font
  (:require [gdx.app :as app]
            [clojure.java.io :as io])
  (:refer-clojure :exclude [find])
  (:import (com.badlogic.gdx.graphics.g2d BitmapFont BitmapFont$TextBounds)
           (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx.files FileHandle)))

(defn font
  [& {:keys [file flip-y?] :as opts}]
  (merge
    opts
    (when file
      {:file (.getPath (io/as-file file))})))

(def cache (atom {}))

(defn load!
  [font]
  (app/on-render-thread
    (or (get @cache font)
        (-> (swap! cache assoc font
                   (if (:file font)
                     (BitmapFont. (FileHandle. (str (:file font)))
                                  (boolean (:flip-y? font)))
                     (BitmapFont. (boolean (:flip-y? font)))))
            (get font)))))

(defn find
  [font]
  (or (get @cache font)
      (load! font)))

(defn unload!
  [font]
  (app/on-render-thread
    (when-some [^BitmapFont f (get @cache font)]
      (swap! cache dissoc font)
      (.dispose f))))

(defn get-bounds
  [font s]
  (let [^BitmapFont gdx-font (find font)
        ^BitmapFont$TextBounds bounds (.getMultiLineBounds gdx-font (str s))]
    [(.-width bounds) (.-height bounds)]))

(defn get-bounds-wrapped
  [font s width]
  (let [^BitmapFont gdx-font (find font)
        ^BitmapFont$TextBounds bounds (.getWrappedBounds gdx-font (str s) (float width))]
    [(.-width bounds) (.-height bounds)]))


(defn set-color-float-bits!
  [^BitmapFont gdx-font float-bits]
  (.setColor gdx-font (float float-bits)))

(defn get-color-float-bits
  [^BitmapFont gdx-font]
  (let [^Color color (.getColor gdx-font)]
    (.toFloatBits color)))

(defmacro using-color-float-bits
  [font float-bits & body]
  `(let [f# (find ~font)
         old-color# (get-color-float-bits f#)]
     (set-color-float-bits! f# ~float-bits)
     (let [r# (do ~@body)]
       (set-color-float-bits! f# old-color#)
       r#)))
