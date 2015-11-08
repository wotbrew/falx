(ns gdx.texture
  (:require [gdx.app :as app]
            [clojure.java.io :as io])
  (:refer-clojure :exclude [find])
  (:import (com.badlogic.gdx.graphics Texture)
           (com.badlogic.gdx.files FileHandle)
           (java.io File)))

(defonce gdx-texture-cache (atom nil))

(defn find-gdx-texture
  [file]
  (let [file (io/as-file file)]
    (get @gdx-texture-cache file)))

(defn load-gdx-texture!
  [file]
  (let [^File file (io/as-file file)
        ^FileHandle file-handle (FileHandle. file)]
    (app/on-render-thread
      (or (find-gdx-texture file)
          (do
            (swap! gdx-texture-cache assoc file (Texture. file-handle))
            (find-gdx-texture file))))))

(defn unload-gdx-texture!
  [file]
  (let [file (io/as-file file)]
    (app/on-render-thread
      (when-some [^Texture t (get @gdx-texture-cache file)]
        (swap! gdx-texture-cache dissoc file)
        (.dispose t)))))

(defn texture
  [file]
  {:file (.getPath (io/as-file file))})

(defonce cache (atom nil))

(defn load!
  [texture]
  (->
    (swap! cache assoc texture
           (load-gdx-texture! (:file texture)))
    (get texture)))

(defn just-find
  [texture]
  (get @cache texture))

(defn find
  [texture]
  (or (just-find texture)
      (load! texture)))

(defn unload!
  [texture]
  (swap! cache dissoc texture)
  (unload-gdx-texture! (:file texture)))