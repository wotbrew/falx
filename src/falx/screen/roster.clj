(ns falx.screen.roster
  (:require [falx.rect :as rect]
            [falx.widget :as widget]
            [falx.theme :as theme]))

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

(defmethod widget/get-click-event :roster/menu-button
  [m game]
  {:type :event/goto-menu})

(defn menu-button
  [rect]
  {:type :roster/menu-button
   :text "(M)enu"
   :rect rect})

;; ================
;; CREATE BUTTON

(derive :roster/create-button :ui/text-button)

(defmethod widget/get-click-event :roster/create-button
  [m game]
  {:type :event/goto-create})

(defn create-button
  [rect]
  {:type :roster/create-button
   :text "(C)reate"
   :rect rect})

;; ==============
;; KILL BUTTON

(derive :roster/kill-button :ui/text-button)

(defmethod widget/process-frame :roster/kill-button
  [m game]
  (assoc m :disabled? (nil? (-> game :roster :selected))))

(defn kill-button
  [rect]
  {:type :roster/kill-button
   :text "(K)ill"
   :rect rect})

;; ===============
;; DETAILS BUTTON

(derive :roster/details-button :ui/text-button)

(defmethod widget/process-frame :roster/details-button
  [m game]
  (assoc m :disabled? (nil? (-> game :roster :selected))))

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
  ([game]
   (screen game (-> game :ui-camera :size)))
  ([game size]
   (let [[width height] size
         [x y w h :as r] (centered-rect size)]
     (-> (widget/panel
           [(widget/filler-border (rect/extend r 32))
            (title-text [x y w 32])
            (buttons [x (+ y 32) w 32])])
         (widget/process-frame
           game)))))