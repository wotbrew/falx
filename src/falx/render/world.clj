(ns falx.render.world
  (:require [falx.space :as space]
            [falx.draw :as draw]
            [falx.sprite :as sprite]
            [falx.world :as world]
            [clj-gdx :as gdx]))

(defn draw-slice!
  [g slice x y w h]
  (let [w (:world g)
        space (:space w)
        ids (space/get-at-slice space slice)]
    (doseq [id ids
            :let [thing (world/get-thing w id)
                  [wx wy :as p] (:point (:cell thing))]
            :when (and (some? p)
                       ;; check bounds
                       )]
      (draw/sprite! sprite/human-male (* 32 wx) (* 32 wy) 32 32))))

(def layers
  [:creature
   nil])

(defn draw!
  ([g [x y w h]]
   (draw! g x y w h))
  ([g x y w h]
   (let [level :limbo
         cpoint (-> g :ui :viewport :camera :point (or [0 0]))]
     (gdx/using-camera
       (assoc gdx/default-camera :point cpoint)
       (doseq [layer layers
               :let [slice (space/slice level layer)]]
         (draw-slice! g slice x y w h))))))