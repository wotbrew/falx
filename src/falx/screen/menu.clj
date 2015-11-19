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
  (-> (widget/label rect ":: FALX ::")
      (assoc :context {:color theme/red})))

(derive :menu/new-button :ui/text-button)
(derive :menu/new-button :ui/nav-button)

(defn new-adventure-button
  [rect]
  {:type :menu/new-button
   :text "(N)ew Adventure"
   :rect rect
   :screen-key :new})

;; =========
;; CONTINUE BUTTON

(derive :menu/continue-button :ui/text-button)
(derive :menu/continue-button :ui/nav-button)

(defmethod widget/on-frame :menu/continue-button
  [m game]
  (assoc m :disabled? (empty? (:saves game))))

(defn continue-adventure-button
  [rect]
  {:type :menu/continue-button
   :text "(C)ontinue Adventure"
   :rect rect
   :screen-key :continue})

;; ===============
;; ROSTER BUTTON

(derive :menu/roster-button :ui/text-button)
(derive :menu/roster-button :ui/nav-button)

(defmethod widget/get-hover-text :menu/roster-button
  [m game]
  (when (:hovering? m)
    "Create and manage your characters"))

(defn roster-button
  [rect]
  {:type :menu/roster-button
   :text "(R)oster"
   :rect rect
   :screen-key :roster})

;; ===============
;; SETTINGS BUTTON

(defn settings-button
  [rect]
  (widget/text-button "(S)ettings" rect))

;; ===============
;; QUIT BUTTON

(defn quit-button
  [rect]
  (widget/text-button "(Q)uit" rect))

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
  [game size]
  (let [[x y w h :as r] (centered-rect size)]
    (-> (widget/panel
          [(widget/filler-border (rect/extend r 32))
           (title-text [x y w 32])
           (buttons [x (+ y 32) w (- h 32)])
           widget/hover-text
           widget/basic-mouse])
        (widget/process-frame game))))

(defmethod widget/get-screen :menu
  [_ game size]
  (screen game size))