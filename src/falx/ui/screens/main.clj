(ns falx.ui.screens.main
  (:require [falx.ui.widgets :as widgets]
            [falx.size :as size]
            [falx.rect :as rect]))



(def button-size [480 32])

(def button-width (size/get-width button-size))

(def button-height (size/get-height button-size))
0
(defn button
  [text & opts]
  (delay (apply widgets/button
           0 0 button-width button-height
           :text text
           opts)))

(def new-adventure
  (button "- New Adventure -" :on-click-fn (constantly :foo)))

(def continue-adventure
  (button "- Continue Adventure -"))

(def roster
  (button "- Roster -"))

(def settings
  (button "- Settings -"))

(def exit
  (button "- Exit -"))

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
      (mapv deref buttons))))

(def get-menu
  (memoize menu))