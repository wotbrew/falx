(ns falx.game
  (:require [falx.levels.testing :as level-testing]
            [clj-gdx :as gdx]
            [falx.point :as point]))

(def default
  {:world        @level-testing/world
   :world-camera gdx/default-camera})

(defmulti act "Have the game react to an action" (fn [game action] (:type action)))

(defmethod act :default
  [game action]
  game)

;; CAMERA

(defn shift-camera
  [game point]
  (update-in game [:world-camera :pos] point/add point))

(defmethod act :action/shift-camera
  [game {:keys [point]}]
  (shift-camera game point))

