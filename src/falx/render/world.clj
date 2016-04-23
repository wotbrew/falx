(ns falx.render.world
  (:require [falx.space :as space]
            [falx.draw :as draw]
            [falx.sprite :as sprite]
            [falx.world :as world]
            [gdx.color :as color]))

(defmulti thing!
  (fn [g id thing x y w h] (:type thing)))

(defmethod thing! :default
  [g id thing x y w h])

(defmethod thing! :creature
  [g id thing x y w h]
  (when (contains? (:selected (:player g)) id)
    (draw/sprite! sprite/selection x y w h {:color color/green}))
  (draw/sprite! sprite/human-male x y w h))

(defmethod thing! :terrain
  [g id thing x y w h]
  (draw/sprite! sprite/castle-floor x y w h))

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
      (thing! g id thing (* 32 wx) (* 32 wy) 32 32))))

(def layers
  [:floor
   :creature
   nil])

(defn level!
  ([g level [x y w h]]
   (level! g level x y w h))
  ([g level x y w h]
   (doseq [layer layers
           :let [slice (space/slice level layer)]]
     (draw-slice! g slice x y w h))))