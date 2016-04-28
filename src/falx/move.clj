(ns falx.move
  (:require [falx.game :as g]
            [falx.point :as point]
            [falx.turn :as turn]
            [falx.action :as action]
            [falx.thing :as thing]))

(defn get-direction-vector
  [direction]
  (if (vector? direction)
    direction
    (case direction
      :nw [-1 -1]
      :n [0 -1]
      :ne [1 -1]
      :w [-1 0]
      :sw [-1 1]
      :s [0 1]
      :se [1 1]
      :e [1 0])))

(defn get-moveable-entity
  [g id]
  (let [e (g/get-entity g id)]
    (when (and e (:point e) (g/active? g id))
      e)))

(defn move
  [g id direction]
  (if-some [e (get-moveable-entity g id)]
    (let [v (get-direction-vector direction)
          target-point (point/add (:point e) v)]
      (if (some thing/solid? (g/get-at g target-point))
        (turn/end g id)
        (-> (g/add-entity g (assoc e :point target-point))
            (turn/end id))))
    (turn/end g id)))

(action/bind :move #'move)

(defn move-player
  [g direction]
  (move g :player direction))

(action/bind :move-player #'move-player)

(defn move-towards
  [g id target]
  (let [origin-point (g/get-point g id)
        target-point (g/get-point g target)]
    (if (and origin-point target-point)
      (move g id (point/direction origin-point target-point))
      g)))

(action/bind :move-towards #'move-towards)