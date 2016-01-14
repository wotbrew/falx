(ns falx.draw.game
  (:require [falx.draw.world :as draw-world]
            [clj-gdx :as gdx]
            [clojure.java.io :as io]))

(def yellow-flag
  (gdx/sprite
    (io/resource "tiles/Misc.png")
    [32 64 32 32]))

(defn draw-path-preview!
  [game]
  (let [level (:level game)]
    (->> (:path-preview game)
         (eduction (comp (filter #(= level (:level %)))
                         (map :point)))
         (run!
           (fn [[x y]]
             (gdx/draw-sprite! yellow-flag
                               (* x draw-world/cell-width)
                               (* y draw-world/cell-height)
                               draw-world/cell-width
                               draw-world/cell-height))))))

(defn draw!
  [game]
  (gdx/using-camera
    (:world-camera game gdx/default-camera)
    (draw-world/draw-level!
      (:world game)
      (:level game))
    (draw-path-preview! game)))