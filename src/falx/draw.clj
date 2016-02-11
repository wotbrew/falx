(ns falx.draw
  (:require [clj-gdx :as gdx])
  (:import (clojure.lang IPersistentMap)))

(defmulti drawm! (fn [m x y] (:type m)))

(defmethod drawm! :default
  [_ x y]
  (gdx/draw-string! "?" x y 32))

(defprotocol IDraw
  (draw! [this x y]))

(extend-protocol IDraw
  nil
  (draw! [this x y] (gdx/draw-string! "?" x y 32))
  IPersistentMap
  (draw! [this x y] (drawm! this x y)))