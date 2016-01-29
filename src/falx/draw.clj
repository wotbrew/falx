(ns falx.draw
  (:import (clojure.lang IPersistentMap)))

(defmulti drawm! :type)

(defprotocol IDraw
  (draw! [this]))

(extend-protocol IDraw
  IPersistentMap
  (draw! [this] (drawm! this)))