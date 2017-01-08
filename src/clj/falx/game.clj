(ns falx.game
  (:require [clojure.core.async :as async])
  (:import (java.util UUID)))

(defprotocol IGame
  (-state [this])
  (-set-state [this gs])
  (-update-state! [this f])
  (-frame [this])
  (-next-frame [this tick])
  (-event-pub [this]))

(deftype Game [id state-ref frame-ref event-chan event-pub]
  IGame
  (-state [this]
    @state-ref)
  (-set-state [this gs]
    (reset! state-ref gs))
  (-update-state! [this f]
    (let [events (volatile! nil)
          ret
          (swap! state-ref (fn [gs]
                             (let [ret (f gs)]
                               (if (seq (:events ret))
                                 (do (vreset! events (:events ret))
                                     (assoc ret :events []))
                                 ret))))]
      (when @events
        (async/onto-chan event-chan @events false))
      ret))
  (-frame [this]
    @frame-ref)
  (-next-frame [this tick]
    (reset! frame-ref
            {:game  id
             :state @state-ref
             :tick  tick}))
  (-event-pub [this]
    event-pub))

(defonce ^:private registry
  (atom {}))

(defn game
  ([]
   (game (UUID/randomUUID)))
  ([id]
    (if-some [g (get @registry id)]
      g
      (get (swap! registry assoc id
                  (let [c (async/chan 128)
                        p (async/pub c :type)]
                    (->Game
                      id
                      (atom {})
                      (atom {})
                      c
                      p)))
           id))))

(extend-protocol IGame
  UUID
  (-state [this]
    (-state (get @registry this)))
  (-set-state [this gs]
    (-set-state (get @registry this) gs))
  (-update-state! [this f]
    (-update-state! (get @registry this) f))
  (-frame [this]
    (-frame (get @registry this)))
  (-next-frame [this tick]
    (-next-frame (get @registry this) tick))
  (-event-pub [this]
    (-event-pub (get @registry this))))

(defn frame
  [g]
  (-frame g))

(defn next-frame
  [g tick]
  (-next-frame g tick))

(defn update-state!
  ([g f]
   (-update-state! g f))
  ([g f & args]
   (update-state! g #(apply f % args))))

(defn set-state!
  [g gs]
  (-set-state g gs))

(defn state
  [g]
  (-state g))

(defn event-pub
  [g]
  (event-pub g))