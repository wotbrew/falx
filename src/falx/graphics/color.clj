(ns falx.graphics.color
  (:import (com.badlogic.gdx.graphics Color)))

(def color-mapping
  {:white Color/WHITE
   :green Color/GREEN
   :yellow Color/YELLOW})

(defn find-color
  [color]
  (get color-mapping color))