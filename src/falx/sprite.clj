(ns falx.sprite
  (:require [clojure.java.io :as io]
            [clj-gdx :as gdx]
            [falx.rect :as rect]))

(defn sprite
  [file x y w h]
  (gdx/sprite file [x y w h]))

(defn get-size
  [sprite]
  (rect/get-size (:rect sprite)))

(defn tile
  [file x y w h]
  (sprite (io/resource (format "tiles/%s.png" file)) x y w h))

(defn tile32
  [file x y]
  (tile file
        (int (* x 32))
        (int (* y 32))
        32
        32))

;; =====
;; Misc
;; =====

(defn misc32
  [x y]
  (tile32 "Misc" x y))

(def selection
  (misc32 0 0))

(def pixel
  (misc32 0 1))

;; =====
;; Races - Human
;; =====

(defn human32
  [x y]
  (tile32 "Human" x y))

(def human-female
  (human32 0 0))

(def human-male
  (human32 1 0))

;; =====
;; Creatures - Goblins
;; =====

(defn goblin32
  [x y]
  (tile32 "Goblins" x y))

(def goblin-worker
  (goblin32 0 0))

;; =====
;; Gui
;; =====

(defn gui32
  [x y]
  (tile32 "Gui" x y))

(def block
  (gui32 0 0))

(def slot
  (gui32 1 0))

;; =====
;; Mouse
;; =====

(defn mouse32
  [x y]
  (tile32 "Mouse" x y))

(def mouse-point
  (mouse32 0 0))

(def mouse-select
  (mouse32 1 0))

(def mouse-attack
  (mouse32 2 0))

(def mouse-gray-attack
  (mouse32 3 0))

(def mouse-spell
  (mouse32 0 1))

(def mouse-gray-spell
  (mouse32 1 1))

(def mouse-left
  (mouse32 2 1))

(def mouse-right
  (mouse32 3 1))

(def mouse-arrow
  (mouse32 0 2))

(def mouse-gray-arrow
  (mouse32 1 2))

;; =====
;; Castle
;; =====

(defn castle32
  [x y]
  (tile32 "CastleDungeon" x y))

(def castle-floor
  (castle32 0 0))

(def castle-wall
  (castle32 0 1))