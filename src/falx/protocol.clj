(ns falx.protocol
  (:import (clojure.lang IFn)))

(defprotocol IRequest
  (-get-response [this])
  (-respond [this g response]))

(extend-protocol IRequest
  IFn
  (-get-response [this]
    (.invoke this))
  (-respond [this g response]
    g))

(defprotocol IAction
  (-perform [this g]))

(extend-protocol IAction
  IFn
  (-perform [this g]
    (.invoke this g)))