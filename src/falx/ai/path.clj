(ns falx.ai.path
  (:require [falx.world :as world]
            [falx.point :as point]
            [clojure.core.async :as async]
            [falx.state :as state]
            [falx.ai.memory :as memory]
            [falx.entity :as entity]
            [falx.event :as event]
            [falx.game.selection :as selection]))

(defn step-behaviour
  [eid]
  (async/go
    (let [memory (memory/recall eid)
          {:keys [path]} memory]
      (when (seq path)
        (state/update-entity! eid entity/put (first path))
        (memory/remember! eid {:path (rest path)})))))

(defn solid-at?
  [world level point]
  (let [es (world/get-entities-with world :cell (entity/cell level point))]
    (boolean (some :solid? es))))

(defn find-path
  [world entity cell]
  (let [{:keys [point level]} cell]
    (when (not (solid-at? world level point))
      (for [point (point/get-a*-path #(not (solid-at? world level %))
                                     (:point entity)
                                     point)]
        (entity/cell level point)))))


(defn path-behaviour
  [eid]
  (async/go
    (let [memory (memory/recall eid)
          {:keys [path move]} memory]
      (cond
        (= move (:cell (state/get-entity eid)))
        (memory/forget! eid :move)
        (not= (last path) move)
        (let [world (:world @state/game)
              entity (world/get-entity world eid)
              path (find-path world entity move)]
          (when (seq path)
            (memory/remember! eid {:path (rest path)})))))))

(defn move!
  [eid cell]
  (memory/remember! eid {:move cell}))

(event/subscribe!
  :floor-selected
  :move
  (fn [{:keys [floor]}]
    (let [cell (:cell floor)
          world (:world @state/game)]
      (doseq [e (selection/get-selected world)]
        (do
          (prn (:id e))
          (move! (:id e) cell))))))