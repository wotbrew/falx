(ns falx.render.ui
  (:require [clj-gdx :as gdx]
            [falx.render.world :as render-world]
            [falx.draw :as draw]
            [falx.sprite :as sprite]))

(defmulti component! (fn [g k x y w h] k))

(defn get-gdx-camera
  [camera]
  (let [[x y] (:point camera [0 0])
        [w h] (:size camera [1024 768])
        x2 (double (+ x (/ w 2)))
        y2 (double (+ y (/ h 2)))]
    (assoc gdx/default-camera :point [x2 y2])))

(defmethod component! :viewport
  [g k x y w h]
  (let [st (-> g :ui :viewport)
        {:keys [camera level]} st]
    (gdx/using-camera
      (get-gdx-camera camera)
      (render-world/level! g level x y w h))))

(defmethod component! :mouse
  [g k bx by bw bh]
  (let [[x y] (:point (:mouse (:ui g))
                [0 0])]
    (draw/sprite! sprite/mouse-point x y 32 32)))

(defn components!
  [g x y w h coll]
  (run! #(component! g % x y w h) coll))

(defmulti screen! (fn [g k x y w h] k))

(defmethod screen! :game
  [g k x y w h]
  (components! g x y w h [:viewport :mouse]))