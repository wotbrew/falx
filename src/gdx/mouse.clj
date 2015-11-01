(ns gdx.mouse
  (:require [clojure.set :as set])
  (:import (com.badlogic.gdx Gdx Input Input$Buttons)))

(defn ^Input  get-input
  []
  (when-some [app Gdx/app]
    (.getInput app)))

(defn get-x
  []
  (if-some [input (get-input)]
    (.getX input)
    0))

(defn get-y
  []
  (if-some [input (get-input)]
    (.getY input)
    0))

(defn get-point
  []
  (if-some [input (get-input)]
    [(.getX input) (.getY input)]
    [0 0]))

(defn gdx-button-pressed?
  [gdx-button]
  (if-some [input (get-input)]
    (.isButtonPressed input gdx-button)
    false))

(def gdx-button->key
  {Input$Buttons/LEFT   :left
   Input$Buttons/MIDDLE :middle
   Input$Buttons/RIGHT  :right
   Input$Buttons/BACK   :back
   Input$Buttons/FORWARD :forward})

(def key->gdx-button
  (reduce-kv #(assoc %1 %3 %2) {} gdx-button->key))

(def all-buttons
  (set (keys key->gdx-button)))

(defn button-pressed?
  [key]
  (gdx-button-pressed? (key->gdx-button key)))

(defn get-mouse
  []
  {:point (get-point)
   :pressed (into #{} (filter button-pressed?) all-buttons)})

(defn get-next-mouse
  [previous-mouse]
  (let [now (get-mouse)]
    (assoc now
      :hit (set/difference (:pressed previous-mouse #{})
                           (:pressed now #{})))))