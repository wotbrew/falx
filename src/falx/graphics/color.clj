(ns falx.graphics.color
  (:import (com.badlogic.gdx.graphics Color)))

(def color-mapping
  {:white Color/WHITE
   :green Color/GREEN
   :yellow Color/YELLOW
   :gray Color/GRAY
   :light-gray Color/LIGHT_GRAY})

(defn find-color
  [color]
  (get color-mapping color))