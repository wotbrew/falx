(ns falx.mouse
  (:require [falx.point :as point]
            [falx.rect :as rect]
            [clojure.set :as set])
  (:import (com.badlogic.gdx Gdx Input$Buttons)))

(def default
  {::point point/default
   ::buttons-pressed #{}
   ::buttons-hit #{}})

(def get-point ::point)

(def get-buttons-pressed ::buttons-pressed)

(def get-buttons-hit ::buttons-hit)

(defn button-pressed?
  [mouse button]
  (contains? (get-buttons-pressed mouse) button))

(defn button-hit?
  [mouse button]
  (contains? (get-buttons-hit mouse) button))

(def left-button ::left)

(def right-button ::right)

(defn left-clicked?
  [mouse]
  (button-hit? mouse left-button))

(defn right-clicked?
  [mouse]
  (button-hit? mouse right-button))

(defn in-rectangle?
  ([mouse rect]
   (let [[x y w h] rect]
     (in-rectangle? mouse x y w h)))
  ([mouse x y w h]
   (rect/contains-point? x y w h (get-point mouse))))

(defn get-current-gdx-point
  []
  [(.getX Gdx/input)
   (.getY Gdx/input)])

(defn refresh-point
  [mouse]
  (assoc mouse ::point (get-current-gdx-point)))

(def gdx-button-mapping
  {left-button  Input$Buttons/LEFT
   right-button Input$Buttons/RIGHT})

(def reverse-gdx-button-mapping
  (reduce-kv #(assoc %1 %3 %2) {} gdx-button-mapping))

(def gdx-buttons (set (keys reverse-gdx-button-mapping)))

(defn gdx-button-pressed?
  [gdx-button]
  (.isButtonPressed Gdx/input gdx-button))

(defn get-current-buttons-pressed
  []
  (into #{} (comp (filter gdx-button-pressed?)
                  (map reverse-gdx-button-mapping)) gdx-buttons))

(defn refresh-buttons
  [mouse]
  (let [previous-pressed (get-buttons-pressed mouse)
        pressed (get-current-buttons-pressed)
        hit (set/difference previous-pressed pressed)]
    (assoc mouse
      ::buttons-pressed pressed
      ::buttons-hit hit)))

(defn refresh
  [mouse]
  (-> mouse
      refresh-point
      refresh-buttons))