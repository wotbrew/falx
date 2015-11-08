(ns falx.screen.menu
  (:require [falx.widget :as widget]
            [falx.rect :as rect]
            [falx.theme :as theme]))

(defn centered-rect
  [size]
  (let [[width height] size]
    (rect/center-rect [0 0 width height] [256 256])))

(defn title-text
  [rect]
  (widget/panel
    [(widget/box rect)
     (-> (widget/label rect ":: FALX ::")
         (assoc :context {:color theme/red}))]))

(defn new-adventure-button
  [rect]
  (widget/text-button "New Adventure" rect))

(defn continue-adventure-button
  [rect]
  (-> (widget/text-button "Continue Adventure" rect)
      (assoc :on-update (fn [m game]
                          (assoc m :disabled? (empty? (:saves game)))))))

(defn roster-button
  [rect]
  (-> (widget/text-button "Roster" rect)
      (assoc :click-event {:type :event/goto-roster})))

(defn settings-button
  [rect]
  (widget/text-button "Settings" rect))

(defn quit-button
  [rect]
  (widget/text-button "Quit" rect))

(defn buttons
  [rect]
  (let [coll [new-adventure-button
              continue-adventure-button
              roster-button
              settings-button
              quit-button]
        rects (rect/fit-vertical rect (count coll))]
    (widget/panel
      (mapv (fn [f rect] (f rect)) coll rects))))

(defn screen
  ([game]
   (screen game (-> game :ui-camera :size)))
  ([game size]
   (let [[x y w h :as r] (centered-rect size)]
     (-> (widget/panel
           [(widget/fps-label [0 0 64 32])
            (widget/filler-border (rect/extend r 32))
            (title-text [x y w 32])
            (buttons [x (+ y 32) w (- h 32)])])
         (widget/update-widget game)))))