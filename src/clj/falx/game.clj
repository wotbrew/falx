(ns falx.game
  (:import (java.util UUID)))

(defprotocol IGame
  (-state [this])
  (-set-state [this gs])
  (-update-state! [this f])
  (-frame [this])
  (-next-frame [this tick]))

(deftype Game [id state-ref frame-ref]
  IGame
  (-state [this]
    @state-ref)
  (-set-state [this gs]
    (reset! state-ref gs))
  (-update-state! [this f]
    (swap! state-ref f))
  (-frame [this]
    @frame-ref)
  (-next-frame [this tick]
    (reset! frame-ref
            {:game  id
             :state @state-ref
             :tick  tick})))

(defonce ^:private registry
  (atom {}))

(defn game
  ([]
   (game (UUID/randomUUID)))
  ([id]
    (if-some [g (get @registry id)]
      g
      (get (swap! registry assoc id
                  (->Game
                    id
                    (atom {})
                    (atom {})))
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
    (-next-frame (get @registry this) tick)))

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