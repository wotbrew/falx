(ns falx.event
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :refer [debug]]))

(defonce event-chan
  (async/chan))

(defonce event-pub
  (async/pub event-chan :type (fn [_] (async/buffer 128))))

(defn publish!
  [event]
  (debug event)
  (async/>!! event-chan event))