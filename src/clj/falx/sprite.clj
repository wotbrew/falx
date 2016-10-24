(ns falx.sprite
  (:require [falx.gdx :as gdx]
            [clojure.java.io :as io])
  (:import (java.util.concurrent ConcurrentHashMap)
           (clojure.lang IFn)))

(def ^:private texture
  (let [m (ConcurrentHashMap.)]
    (fn [f]
      (or (.get m f)
          @(gdx/run
             (let [t (gdx/texture f)]
               (.put m f t)
               t))))))

(def ^:private texture-region
  (let [m (ConcurrentHashMap.)]
    (fn [spr]
      (or (.get ^ConcurrentHashMap m spr)
          @(gdx/run
             (let [{:keys [f x y w h]} spr
                   t (texture f)
                   tr (gdx/texture-region t x y w h)]
               (.put m spr tr)
               tr))))))

(defn draw!
  ([spr x y]
    (gdx/draw! (texture-region spr) x y (:w spr) (:h spr)))
  ([spr x y w h]
    (gdx/draw! (texture-region spr) x y w h)))

(defrecord SingleSprite [f x y w h]
  IFn
  (invoke [this v]
    (case (count v)
      2 (draw! this (nth v 0) (nth v 1))
      4 (draw! this (nth v 0) (nth v 1) (nth v 2) (nth v 3))
      (throw (Exception. (str "Unsupported vector length: " (count v))))))
  (invoke [this x2 y2]
    (draw! this x2 y2))
  (invoke [this x2 y2 w2 h2]
    (draw! this x2 y2 w2 h2)))

(defn sprite
  [f x y w h]
  (->SingleSprite f x y w h))

(defn tile32
  [f x y]
  (sprite (io/file (io/resource (str "tiles/" f))) (* x 32) (* y 32) 32 32))

(def human32
  (partial tile32 "Human.png"))

(def human-female!
  (human32 0 0))

(def human-male!
  (human32 1 0))

(def mouse32
  (partial tile32 "Mouse.png"))

(def mouse!
  (mouse32 0 0))

(def mouse-open!
  (mouse32 1 0))

(def mouse-attack!
  (mouse32 2 0))

(def mouse-attack-grey!
  (mouse32 3 0))

(def mouse-cast!
  (mouse32 0 1))

(def mouse-cast-grey!
  (mouse32 1 1))

(def decor32
  (partial tile32 "Decor.png"))

(def misc32
  (partial tile32 "Misc.png"))

(def selection!
  (misc32 0 0))