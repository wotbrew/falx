(ns falx.screen.roster
  (:require [falx.rect :as rect]
            [falx.widget :as widget]
            [falx.theme :as theme]))

(defn get-state
  [game]
  (-> game :ui :roster))

(defn update-state
  [game f & args]
  (update-in game [:ui :roster] #(apply f % args)))

(defn centered-rect
  [size]
  (let [[width height] size]
    (rect/center-rect [0 0 width height] [640 480])))

(defn title-text
  [rect]
  (-> (widget/label rect ":: ROSTER ::")
      (assoc :context {:color theme/red})))

;; =================
;; MENU BUTTON

(derive :roster/menu-button :ui/text-button)
(derive :roster/menu-button :ui/nav-button)

(defn menu-button
  [rect]
  {:type :roster/menu-button
   :text "(M)enu"
   :rect rect
   :screen-key :menu})

;; ================
;; CREATE BUTTON

(derive :roster/create-button :ui/text-button)
(derive :roster/create-button :ui/nav-button)

(defn create-button
  [rect]
  {:type :roster/create-button
   :text "(C)reate"
   :rect rect
   :screen-key :create})

;; ==============
;; KILL BUTTON

(derive :roster/kill-button :ui/text-button)

(defmethod widget/on-frame :roster/kill-button
  [m game]
  (assoc m :disabled? (nil? (:selected (get-state game)))))

(defn kill-button
  [rect]
  {:type :roster/kill-button
   :text "(K)ill"
   :rect rect})

;; ===============
;; DETAILS BUTTON

(derive :roster/details-button :ui/text-button)

(defmethod widget/on-frame :roster/details-button
  [m game]
  (assoc m :disabled? (nil? (:selected (get-state game)))))

(defn details-button
  [rect]
  {:type :roster/details-button
   :text "(D)etails"
   :rect rect})

(defn buttons
  [rect]
  (let [coll [create-button
              details-button
              kill-button
              menu-button]
        rects (rect/fit-horizontal rect (count coll))]
    (widget/panel
      (mapv (fn [f rect] (f rect)) coll rects))))

(defn screen
  [game size]
  (let [[width height] size
        [x y w h :as r] (centered-rect size)]
    (-> (widget/panel
          [(widget/filler-border (rect/extend r 32))
           (title-text [x y w 32])
           (buttons [x (+ y 32) w 32])
           widget/hover-text
           widget/basic-mouse])
        (widget/process-frame
          game))))

(defmethod widget/get-screen :roster
  [_ game size]
  (screen game size))