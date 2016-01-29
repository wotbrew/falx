(ns falx.draw.sprite
  (:require [clj-gdx :as gdx]
            [clojure.java.io :as io]))

(defn sprite
  [file x y w h]
  (gdx/sprite file [x y w h]))

(defn tile
  [file x y w h]
  (sprite (io/resource (format "tiles/%s.png" file)) x y w h))

(defn tile32
  [file x y]
  (tile file
        (int (* x 32))
        (int (* y 32))
        32
        32))

(defn misc32
  [x y]
  (tile32 "Misc" x y))

(def pixel
  (misc32 0 1))

(defn human32
  [x y]
  (tile32 "Human" x y))

(def human-female
  (human32 0 0))

(def human-male
  (human32 1 0))

