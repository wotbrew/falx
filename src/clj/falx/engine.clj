(ns falx.engine
  (:require [falx.gdx :as gdx]
            [clojure.set :as set])
  (:import (clojure.lang IDeref)))

(defrecord GameState
  [ui-state
   model-state
   settings])

(defrecord UIState
  [])

(defrecord Settings
  [])

(defrecord InputState
  [mouse
   hit
   down])

(deftype Game [agent]
  IDeref
  (deref [this]
    @agent))

(defrecord Frame
  [game-state
   input-state
   delta-time
   elapsed-time])

(defn current-time
  []
  (System/nanoTime))

(defn game
  [gs]
  (->Game (agent gs :error-mode :continue)))

;; Input

(defn next-input
  [previous-input input]
  (assoc input
    :hit (set/difference (:down previous-input)
                         (:down input))))

(def current-input
  (let [keymap (some-fn
                 (into {} (for [k (keys gdx/keyboard-keys)]
                            [k (keyword "key" (name k))]))
                 identity)
        btnmap (some-fn
                 (into {} (for [k (keys gdx/buttons)]
                            [k (keyword "button" (name k))]))
                 identity)]
    (fn []
      (if gdx/*on-thread*
        (map->InputState
          {:mouse (gdx/mouse)
           :down  (into #{} cat [(mapv keymap (gdx/keys-pressed))
                                 (mapv btnmap (gdx/buttons-pressed))])
           :hit   #{}})
        @(gdx/run (current-input))))))

(defn inputted?
  [input binding]
  (case (first binding)
    :and (every? (partial inputted? input) (next binding))
    :or (some (partial inputted? input) (next binding))
    :hit (every? (partial contains? (:hit input)) (next binding))
    :down (every? (partial contains? (:down input)) (next binding))))

;; Frame

(defn frame
  [gs input elapsed-time]
  (->Frame gs
           input
           0
           elapsed-time))

(defn next-frame
  [previous-frame gs input elapsed-time]
  (let [pet (:elapsed-time previous-frame 0)
        pin (:input previous-frame)]
    (->Frame gs
             (next-input pin input)
             (if (pos? pet)
               (- elapsed-time pet)
               0)
             elapsed-time)))