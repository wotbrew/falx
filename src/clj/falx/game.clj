(ns falx.game
  (:require [clojure.core.async :as async])
  (:import (java.lang AutoCloseable)))

(defrecord Game [id
                 state-ref
                 event-pub
                 event-chan
                 request-chan]
  AutoCloseable
  (close [this]
    (async/close! event-chan)
    (async/close! request-chan)))

;; watcher
;; push events
;; spawn request handlers

(defn frame
  [g]
  ;; get input
  ;; handle input
  ;; draw
  )

(defonce disk-saved-games (atom []))

(defn saved-games
  []
  (if (= :uninitialized @disk-saved-games)
    (locking disk-saved-games
      ;; look at meta on disk
      )
    @disk-saved-games))

(defn save!
  [g]
  ;; serialize game
  ;; add to disk-saved-games atom
  )

(defn load
  [saved-game]
  ;; restore a saved game
  ;; returns a new Game instance
  )
