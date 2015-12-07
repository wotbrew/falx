(ns falx.game)

(defmulti act "Have the game react to an action" (fn [game action] (:type action)))

(defmethod act :default
  [game action]
  action)

