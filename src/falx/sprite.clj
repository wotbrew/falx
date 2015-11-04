(ns falx.sprite
  (:require [clj-gdx :as gdx]
            [clojure.java.io :as io]))

(defn tiles
  [name]
  (io/resource (format "tiles/%s.png" name)))

(defn sprite
  [tile x y]
  (gdx/sprite
    (tiles tile)
    [(* x 32) (* y 32) 32 32]))

(def misc* (partial sprite "Misc"))

(def circle
  (misc* 0 0))

(def pixel
  (misc* 1 0))

(def bread
  (misc* 1 1))

(def gold
  (misc* 2 1))

(def green-flag
  (misc* 0 2))

(def yellow-flag
  (misc* 1 2))

(def red-flag
  (misc* 2 2))

(def gui* (partial sprite "Gui"))

(def blank
  (gui* 0 0))

(def block
  (gui* 1 0))

(def helm
  (gui* 2 0))

(def character
  (gui* 3 0))

(def inventory
  (gui* 4 0))

(def book
  (gui* 5 0))

(def bulb
  (gui* 6 0))

(def wait
  (gui* 7 0))

(def map-scroll
  (gui* 8 0))

(def scroll-up
  (gui* 0 1))

(def scroll-down
  (gui* 1 1))

(def turn
  (gui* 2 1))

(def combat
  (gui* 3 1))

(def peace
  (gui* 4 1))

(def potion
  (gui* 5 1))

(def boost
  (gui* 6 1))

(def scroll
  (gui* 7 1))

(def mouse* (partial sprite "Mouse"))

(def mouse
  (mouse* 0 0))

(def mouse-select
  (mouse* 1 0))

(def mouse-attack
  (mouse* 2 0))

(def mouse-attack-grey
  (mouse* 3 0))



