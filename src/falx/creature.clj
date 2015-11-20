(ns falx.creature
  (:require [falx.race :as race]
            [falx.draw :as draw])
  (:import (java.util UUID)))

(defn creature
  ([name gender race]
    (creature name gender race #{}))
  ([name gender race traits]
   {:id     (str (UUID/randomUUID))
    :type   :entity/creature
    :name   name
    :gender gender
    :race   race
    :traits (set traits)}))

(defn creature?
  [creature]
  (= (:type creature) :entity/creature))

(defn move
  [creature position]
  (assoc creature
    :position position
    :point (:point position)
    :map (:map position)))

(defn get-sprites
  [creature]
  [(race/get-body-sprite (:race creature) (:gender creature))])

(defmethod draw/thing! :entity/creature
  [creature rect]
  (run! #(draw/sprite! % rect) (get-sprites creature)))