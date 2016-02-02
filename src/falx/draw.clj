(ns falx.draw
  (:import (clojure.lang IPersistentMap)))

(def drawm! nil)

(defmulti drawm! (fn [m x y] (:type m)))

(defmethod drawm! :default
  [_ _ _])

(defprotocol IDraw
  (draw! [this x y]))

(extend-protocol IDraw
  IPersistentMap
  (draw! [this x y] (drawm! this x y)))