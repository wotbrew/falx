(ns falx.theme
  (:require [clj-gdx :as gdx]
            [gdx.color :as color]))

(def font gdx/default-font)

(def white color/white)

(def light-gray color/light-gray)

(def gray color/gray)

(def dark-gray color/dark-gray)

(def green color/green)

(def mult color/mult)

(def dark-green (mult color/green color/gray))

(def yellow color/yellow)

(def red color/red)