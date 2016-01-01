(ns falx.draw.thing
  (:require [clj-gdx :as gdx]
            [clojure.java.io :as io]))

(defmulti draw! (fn [thing x y w h] (:type thing)))

(defmethod draw! :default
  [thing x y w h]
  (gdx/draw-string! "?" x y w))

(defmethod draw! :creature
  [thing x y w h]
  (gdx/draw-string! "@" x y w))