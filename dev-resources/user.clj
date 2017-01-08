(ns user
  (:require [falx.game :as g]
            [falx.core :as core]
            [falx.game-state :as gs]))

(defn gs
  []
  (g/state core/playing))

(defn entity
  [id]
  (gs/entity (gs) id))

(defn active-party
  []
  (gs/active-party-entity (gs)))