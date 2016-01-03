(ns falx.game.camera
  (:require [falx.game :as game]
            [falx.point :as point]
            [falx.input :as input]))

(def camera-mult 1.0)

(def camera-fast-mult 2.5)

(def camera-delta-factor 250)

(defn get-speed
  [game]
  (* (:delta game 0.0)
     camera-delta-factor
     camera-mult
     (if (input/modified? (:input game))
       camera-fast-mult
       1.0)))

(defn get-delta-point
  [game direction]
  (point/scale direction (get-speed game)))

(defn move-in-direction
  [game direction]
  (let [dt (get-delta-point game direction)]
    (update-in game [:world-camera :point] point/add dt)))

(game/defreaction!
  [:event.action :action.pressed/cam-up]
  ::cam-up
  (fn [game _]
    (move-in-direction game point/north)))

(game/defreaction!
  [:event.action :action.pressed/cam-left]
  ::cam-left
  (fn [game _]
    (move-in-direction game point/west)))

(game/defreaction!
  [:event.action :action.pressed/cam-down]
  ::cam-down
  (fn [game _]
    (move-in-direction game point/south)))

(game/defreaction!
  [:event.action :action.pressed/cam-right]
  ::cam-right
  (fn [game _]
    (move-in-direction game point/east)))

(defn get-world-point
  [game point]
  (let [cam (:world-camera game)]
    (gdx.camera/get-world-point cam point)))

(defn get-world-mouse-point
  [game]
  (let [point (input/get-mouse-point (:input game))]
    (get-world-point game point)))

(def cell-width 32)

(def cell-height 32)

(defn get-world-mouse-level-point
  [game]
  (let [[x y] (get-world-mouse-point game)]
    [(int (/ x cell-width))
     (int (/ y cell-height))]))