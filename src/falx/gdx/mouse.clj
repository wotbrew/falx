(ns falx.gdx.mouse
  (:require [falx.gdx.impl.signal :as signal])
  (:import (com.badlogic.gdx Input Gdx Input$Buttons)))

(defn- ^Input input
  []
  (when-some [app Gdx/app]
    (.getInput app)))

(def point
  (signal/signal
    (if-some [input (input)]
      [(.getX input) (.getY input)]
      [0 0])))

(def buttons
  {::button.left Input$Buttons/LEFT
   ::button.middle Input$Buttons/MIDDLE
   ::button.right Input$Buttons/RIGHT
   ::button.back Input$Buttons/BACK
   ::button.forward Input$Buttons/FORWARD})

(def delta
  (signal/signal
    (if-some [input (input)]
      [(.getDeltaX input) (.getDeltaY input)]
      [0.0 0.0])))

(defn pressed?
  [button]
  (if-some [input (input)]
    (.isButtonPressed input (buttons button))
    false))

(defn pressed
  []
  (into #{} (filter pressed?) (keys buttons)))