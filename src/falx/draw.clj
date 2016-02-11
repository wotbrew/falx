(ns falx.draw
  (:require [clj-gdx :as gdx])
  (:import (clojure.lang IPersistentMap IPersistentVector)))

(defmulti drawm! (fn [m x y] (:type m)))

(defmethod drawm! :default
  [_ x y]
  (gdx/draw-string! "?" x y 32))

(defmulti drawv! (fn [v x y] (nth v 0)))

(defmethod drawv! :default
  [_ x y]
  (gdx/draw-string! "?" x y 32))

(defprotocol IDraw
  (draw! [this x y]))

(extend-protocol IDraw
  nil
  (draw! [this x y] (gdx/draw-string! "?" x y 32))
  IPersistentMap
  (draw! [this x y] (drawm! this x y))
  IPersistentVector
  (draw! [this x y]
    (let [[_ config children] this
          [x2 y2 w h] (:rect config [0 0 0 0])
          x' (+ x x2)
          y' (+ y y2)]
      (drawv! this x y)
      (when (sequential? children)
        (run! #(draw! % x' y') children)))))