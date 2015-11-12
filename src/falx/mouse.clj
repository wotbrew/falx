(ns falx.mouse
  (:require [falx.rect :as rect]))

(defn in?
  [mouse rect]
  (rect/contains-point? rect (:point mouse)))

(defn clicked?
  [mouse]
  (contains? (:hit mouse) :left))

(defn rect
  [mouse]
  (let [[x y] (:point mouse)]
    [x y 32 32]))