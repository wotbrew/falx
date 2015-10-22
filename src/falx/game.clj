(ns falx.game
  (:require [falx.event :as event]))

(def default {})

(def game (atom default))

(event/register-handler!
  :event/update-game
  :update-game
  (fn [event]
    (let [f (:f event)]
      (swap! game f))))

(defn update-game-event
  ([f]
    {:event/type :event/update-game
     :f f})
  ([f & args]
    (update-game-event #(apply f % args))))