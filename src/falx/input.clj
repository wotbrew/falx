(ns falx.input
  (:require [clj-gdx :as gdx]
            [falx.rect :as rect]))

(defn get-current-input
  []
  {:mouse    @gdx/mouse-state
   :keyboard @gdx/keyboard-state})

(defn mouse-in?
  [input rect]
  (rect/contains-point? rect (-> input :mouse :point)))

(defn mouse-button-hit?
  ([input button]
   (contains? (-> input :mouse :hit) button))
  ([input button rect]
   (and (mouse-button-hit? input button)
        (mouse-in? input rect))))

(defn clicked?
  ([input]
   (mouse-button-hit? input :left))
  ([input rect]
   (mouse-button-hit? input :left rect)))

(defn key-hit?
  [input key]
  (contains? (-> input :keyboard :hit) key))

