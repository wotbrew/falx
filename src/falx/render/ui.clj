(ns falx.render.ui
  (:require [clj-gdx :as gdx]
            [falx.render.world :as render-world]
            [falx.draw :as draw]
            [falx.sprite :as sprite]
            [falx.screen :as screen]
            [falx.ui :as ui]))

(defmulti element! (fn [g element x y w h] (:type element)))

(defmethod element! :default
  [g element x y w h])

(defn get-gdx-camera
  [camera]
  (let [[x y] (:point camera [0 0])
        [w h] (:size camera [1024 768])
        x2 (double (+ x (/ w 2)))
        y2 (double (+ y (/ h 2)))]
    (assoc gdx/default-camera :point [x2 y2])))

(defmethod element! :viewport
  [g element x y w h]
  (let [st (ui/get-state g element)
        {:keys [camera level]} st]
    (gdx/using-camera
      (get-gdx-camera camera)
      (render-world/level! g level x y w h))))

(defmethod element! :mouse
  [g element bx by bw bh]
  (let [[x y] (:point (:mouse (:ui g))
                [0 0])]
    (draw/sprite! sprite/mouse-point x y 32 32)))

(defn draw!
  [g element]
  (when-some [[x y w h] (:rect element)]
    (element! g element x y w h))
  (run! #(draw! g %) (:children element)))