(ns falx.draw.thing
  (:require [clj-gdx :as gdx]
            [clojure.java.io :as io]))

(defmulti draw! (fn [thing x y w h] (:type thing)))

(defmethod draw! :default
  [thing x y w h]
  (gdx/draw-string! "?" x y w))

(def human-male
  (gdx/sprite
    (io/resource "tiles/Human.png")
    [32 0 32 32]))

(def selection-circle
  (gdx/sprite
    (io/resource "tiles/Misc.png")
    [0 0 32 32]))

(defmethod draw! :creature
  [thing x y w h]
  (when (:selected? thing)
    (gdx/draw-sprite! selection-circle x y w h {:color gdx.color/green}))
  (gdx/draw-sprite! human-male x y w h))