(ns falx.party
  (:require [falx.game-state :as gs]
            [falx.character :as char]
            [falx.gdx :as gdx]
            [falx.ui :as ui])
  (:import (com.badlogic.gdx.graphics Color)))

(defn draw-model!
  [gs e x y w h]
  (when-not (:dead? e)
    (char/draw-body! e x y w h)))

(defn draw-models!
  [gs x y w h ents]
  (let [w (* w 0.75)
        h (* h 0.75)
        m1 (long (/ w 3))
        m2 (long (/ h 3.3))
        idx (volatile! -1)
        x (+ x (* m1 0.55))
        y (+ y (* m2 0.1))]
    (doseq [e ents]
      (case (vswap! idx inc)
        0 (draw-model! gs e (+ x m1) (- y m2) w h)
        1 (draw-model! gs e x (- y m2) w h)
        2 (draw-model! gs e (- x m1) (- y m2) w h)
        3 (draw-model! gs e (+ x m1) (+ y m2) w h)
        4 (draw-model! gs e x (+ y m2) w h)
        5 (draw-model! gs e (- x m1) (+ y m2) w h)
        nil))))

(defn draw!
  [gs e x y w h]
  (when-some [ents (seq (map (partial gs/pull gs)
                             (if (:player-party? e)
                               (:players gs)
                               (gs/query gs :party (:id e)))))]
    (draw-models! gs x y w h ents)))
