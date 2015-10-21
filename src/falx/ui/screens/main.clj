(ns falx.ui.screens.main
  (:require [falx.ui.widgets :as widgets]
            [falx.size :as size]
            [falx.rect :as rect]))



(def button-size [480 32])

(def button-width (size/get-width button-size))
(def button-height (size/get-height button-size))

(def new-adventure
  (widgets/text-button "- New Adventure -" 0 0 button-width button-height))

(def continue-adventure
  (widgets/text-button "- Continue Adventure -" 0 0 button-width button-height))

(def roster
  (widgets/text-button "- Roster -" 0 0 button-width button-height))

(def settings
  (widgets/text-button "- Settings -" 0 0 button-width button-height))

(def exit
  (widgets/text-button "- Exit -" 0 0 button-width button-height))

(def buttons
  [new-adventure
   continue-adventure
   roster
   settings
   exit])

(def menu-box-size
  [(size/get-width button-size)
   (* (size/get-height button-size) (count buttons))])

(defn menu-box-rect
  [screen-size]
  (size/get-centered-rect screen-size menu-box-size))

(defn menu
  [screen-size]
  (let [[x y] (menu-box-rect screen-size)]
    (widgets/vertical-panel
      button-height x y
      buttons)))

(def get-menu
  (memoize menu))