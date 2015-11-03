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

(def black (gdx-color->map Color/BLACK))

(def blue (gdx-color->map Color/BLUE))

(def clear (gdx-color->map Color/CLEAR))

(def cyan (gdx-color->map Color/CYAN))

(def dark-gray (gdx-color->map Color/DARK_GRAY))

(def gray (gdx-color->map Color/GRAY))

(def green (gdx-color->map Color/GREEN))

(def light-gray (gdx-color->map Color/LIGHT_GRAY))

(def magenta (gdx-color->map Color/MAGENTA))

(def maroon (gdx-color->map Color/MAROON))

(def navy (gdx-color->map Color/NAVY))

(def olive (gdx-color->map Color/OLIVE))

(def orange (gdx-color->map Color/ORANGE))

(def pink (gdx-color->map Color/PINK))

(def purple (gdx-color->map Color/PURPLE))

(def red (gdx-color->map Color/RED))

(def teal (gdx-color->map Color/TEAL))

(def white (gdx-color->map Color/WHITE))

(def yellow (gdx-color->map Color/YELLOW))

