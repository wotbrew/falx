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
    :faction :faction/common
    :name   name
    :gender gender
    :race   race
    :traits (set traits)
    :solid? true}))

(defn creature?
  [creature]
  (= (:type creature) :entity/creature))

(defn get-sprites
  [creature]
  [(race/get-body-sprite (:race creature) (:gender creature))])

(defmethod draw/thing! :entity/creature
  [creature rect]
  (run! #(draw/sprite! % rect) (get-sprites creature)))

(defn concious?
  [creature]
  true)

(defn select
  [creature]
  (assoc creature :selected? true))

(defn unselect
  [creature]
  (dissoc creature :selected?))
