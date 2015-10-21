(ns falx.mouse
  (:require [falx.point :as point]
            [falx.rect :as rect])
  (:import (com.badlogic.gdx Gdx)))

(defn get-current-gdx-point
  []
  [(.getX Gdx/input)
   (.getY Gdx/input)])

(def default
  {::point point/default
   ::buttons-pressed #{}
   ::buttons-hit #{}})

(def get-point ::point)

(def get-buttons-pressed ::buttons-pressed)

(def get-buttons-hit ::buttons-hit)

(defn in-rectangle?
  ([mouse rect]
   (let [[x y w h] rect]
     (in-rectangle? mouse x y w h)))
  ([mouse x y w h]
   (rect/contains-point? x y w h (get-point mouse))))

(defn update-point
  [mouse]
  (assoc mouse ::point (get-current-gdx-point)))