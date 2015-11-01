(ns gdx.color
  (:refer-clojure :exclude [find compile])
  (:import (com.badlogic.gdx.graphics Color)))

(defn get-float-bits
  [color]
  (Color/toFloatBits
    (float (:red color))
    (float (:green color))
    (float (:blue color))
    (float (:alpha color))))

(defn get-gdx-color
  [color]
  (Color.
    (float (:red color))
    (float (:green color))
    (float (:blue color))
    (float (:alpha color))))

(defn compile
  [color]
  (assoc color
    :float-bits (get-float-bits color)))

(defn color
  [red green blue alpha]
  (compile
    {:red   red
     :green green
     :blue  blue
     :alpha alpha}))

(defn gdx-color->map
  [gdx-color]
  (color (.-r gdx-color)
         (.-g gdx-color)
         (.-b gdx-color)
         (.-a gdx-color)))

(def white (gdx-color->map Color/WHITE))

(def yellow (gdx-color->map Color/YELLOW))