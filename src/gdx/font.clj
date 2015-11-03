(ns gdx.font
  (:require [gdx.app :as app]
            [clojure.java.io :as io])
  (:refer-clojure :exclude [find])
  (:import (com.badlogic.gdx.graphics.g2d BitmapFont BitmapFont$TextBounds)))

(defn font
  [& {:keys [file flip-y?] :as opts}]
  (merge
    {:type :resource/font}
    opts
    (when file
      {:file (.getPath (io/as-file file))})))

(defonce cache (atom {}))

(defn load!
  [font]
  (app/on-render-thread
    (or (get @cache font))
    (-> (swap! cache assoc font (BitmapFont. (boolean (:flip-y? font))))
        (get font))))

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