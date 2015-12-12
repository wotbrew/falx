(ns falx.draw.entity
  (:require [clj-gdx :as gdx]
            [clojure.java.io :as io]
            [gdx.color :as color]))

(defmulti draw! (fn [entity x y w h] (:type entity)))

(defmethod draw! :default
  [entity x y w h]
  (gdx/draw-string! "?" x y w))

(def castle-floor
  (gdx/sprite (io/resource "tiles/CastleDungeon.png") [0 0 32 32]))

(defmethod draw! :entity/floor
  [entity x y w h]
  (gdx/draw-sprite! castle-floor x y w h))

(def castle-wall1
  (gdx/sprite (io/resource "tiles/CastleDungeon.png") [0 32 32 32]))

(def castle-wall2
  (gdx/sprite (io/resource "tiles/CastleDungeon.png") [32 32 32 32]))

(def castle-wall3
  (gdx/sprite (io/resource "tiles/CastleDungeon.png") [64 32 32 32]))

(def castle-wall4
  (gdx/sprite (io/resource "tiles/CastleDungeon.png") [96 32 32 32]))

(def castle-wall5
  (gdx/sprite (io/resource "tiles/CastleDungeon.png") [128 32 32 32]))

(def castle-walls
  [castle-wall1
   castle-wall2
   castle-wall3
   castle-wall4
   castle-wall5])

(defn get-castle-wall
  [id]
  (nth castle-walls (mod (hash id) (count castle-walls))))

(defmethod draw! :entity/wall
  [entity x y w h]
  (let [id (:id entity)]
    (gdx/draw-sprite! (get-castle-wall id) x y w h)))

(def human-male
  (gdx/sprite (io/resource "tiles/Human.png") [32 0 32 32]))

(def selection
  (gdx/sprite (io/resource "tiles/Misc.png") [0 0 32 32]))

(defmethod draw! :entity/creature
  [entity x y w h]
  (when (:selected? entity)
    (gdx/draw-sprite! selection x y w h {:color color/green}))
  (gdx/draw-sprite! human-male x y w h))