(ns falx.game.path
  (:require [falx.point :as point]
            [falx.event :as event]
            [falx.game.solid :as solid]))

(defn get-path
  [world level point-a point-b]
  (seq (rest (point/get-a*-path (complement (solid/get-solid-point-pred world level)) point-a point-b))))

