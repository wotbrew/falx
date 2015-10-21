(ns falx.size
  (:require [falx.rect :as rect]))

(def default [0 0])

(def get-width #(nth % 0))

(def get-height #(nth % 1))

(defn get-centered-point
  ([size1 size2]
   (let [[w2 h2] size2]
     (get-centered-point size1 w2 h2)))
  ([size w2 h2]
   (let [[w h] size]
     (get-centered-point w h w2 h2)))
  ([w h w2 h2]
   (rect/get-centered-rect-point 0 0 w h w2 h2)))

(defn get-centered-rect
  ([size1 size2]
   (let [[w2 h2] size2]
     (get-centered-rect size1 w2 h2)))
  ([size w2 h2]
   (let [[w h] size]
     (get-centered-rect w h w2 h2)))
  ([w h w2 h2]
   (rect/get-centered-rect 0 0 w h w2 h2)))