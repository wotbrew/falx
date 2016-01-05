(ns falx.event
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :refer [debug]]
            [falx.util :as util]))

(defonce event-chan
  (async/chan))

(defonce event-pub
  (async/pub event-chan :type (fn [_] (async/buffer 128))))

(defn- unregister
  [m key]
  (if-some [event (-> m :key (get key))]
    (-> (util/dissoc-in m [:event event key])
        (util/dissoc-in [:key key]))
    m))

(defn- register
  [m event-type key f]
  (-> (unregister m key)
      (assoc-in [:event event-type key] f)
      (assoc-in [:key key] event-type)))

(defn get-handlers
  [m event-type]
  (-> m :event (get event-type) vals))

(defn handle!
  [m event]
  (let [handlers (get-handlers m (:type event))]
    (run! #(% event) handlers)))

(defonce ^:private handlers
  (atom {}))

(defn defhandler
  [event-type key f]
  (swap! handlers register event-type key f))

(defn defhandler-async
  [event-type key f]
  (defhandler event-type key #(future (f %))))

(defn publish!
  [event]
  (async/>!! event-chan event)
  (handle! @handlers event))