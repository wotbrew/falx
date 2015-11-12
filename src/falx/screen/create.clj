(ns falx.screen.create
  (:require [falx.widget :as widget]
            [falx.theme :as theme]
            [falx.rect :as rect]
            [falx.sprite :as sprite]
            [falx.race :as race]
            [falx.action :as action]))

(defn centered-rect
  [size]
  (let [[width height] size]
    (rect/center-rect [0 0 width height] [640 480])))

(defn title-text
  [rect]
  (-> (widget/label rect ":: CREATE ::")
      (assoc :context {:color theme/red})))

;; =============
;; CANCEL BUTTON

(derive :create/cancel-button :ui/text-button)

(defmethod widget/get-click-event :create/cancel-button
  [m game]
  {:type :event/goto-roster})

(defn cancel-button
  [rect]
  {:type :create/cancel-button
   :text "Cancel (Esc)"
   :rect rect})

;;=============
;; CREATE BUTTON

(derive :create/ok-button :ui/text-button)

(defn ok-button
  [rect]
  {:type :create/ok-button
   :text "Ok (Enter)"
   :rect rect})

;; ==============

(defn bottom-right-buttons
  [rect]
  (let [coll [ok-button
              cancel-button]
        rects (rect/fit-horizontal rect (count coll))]
    (widget/panel
      (mapv (fn [f rect] (f rect)) coll rects))))

(defn name-label
  [rect]
  (widget/label rect "Name:"))

(defn name-input-box
  [rect]
  (widget/text-input-box rect))

(defn name-row
  [rect]
  (let [[x y w h] rect]
    (widget/panel
      [(name-label [x y 64 h])
       (name-input-box [(+ x 64 16) y (- w 64 16) h])])))

(defn gender-label
  [rect]
  (widget/label rect "Gender:"))

;; ============
;; GENDERS

(action/defreaction
  ::set-gender
  :set-gender
  (fn [game action]
    (assoc-in game [::state :gender] (:gender action :male))))

;; =============
;; MALE BUTTON

(derive :ui/male-button :ui/text-button)

(defmethod widget/on-frame :ui/male-button
  [m game]
  (assoc m :selected? (-> game ::state :gender (= :male))))

(defmethod widget/get-click-action :ui/male-button
  [m game]
  {:type ::set-gender
   :gender :male})

(defn male-button
  [rect]
  {:type :ui/male-button
   :text "M"
   :rect rect})

;; =============
;; FEMALE BUTTON

(derive :ui/female-button :ui/text-button)

(defmethod widget/on-frame :ui/female-button
  [m game]
  (assoc m :selected? (-> game ::state :gender (not= :male))))

(defmethod widget/get-click-action :ui/female-button
  [m game]
  {:type ::set-gender
   :gender :female})

(defn female-button
  [rect]
  {:type :ui/female-button
   :text "F"
   :rect rect})

(defn gender-row
  [rect]
  (let [[x y w h] rect]
    (widget/panel
      [(gender-label [x y 64 h])
       (male-button [(+ x 16 64) y 64 32])
       (female-button [(+ x 1 16 64 64) y 64 32])])))

(defn race-row
  [rect]
  (let [[x y w h] rect]
    (widget/panel
      [(widget/label [x y 64 h] "Race:")])))

(defn race-button
  [rect game race]
  {:type :ui/sprite
   :sprite (race/get-body-sprite race (::state game))
   :rect rect})

(defn screen
  ([game]
   (screen game (-> game :ui-camera :size)))
  ([game size]
   (let [[x y w h :as r] (centered-rect size)]
     (-> (widget/panel
           [(widget/filler-border (rect/extend r 32))
            (title-text [x y w 32])
            (name-row [x (+ y 32) (+ 64 256) 32])
            (gender-row [(+ x 96 256) (+ y 32) (+ 64 256) 32])
            (race-row [x (+ y 64 16) w 32])
            (bottom-right-buttons [(+ x w -256) (+ y h -32) 256 32])
            widget/basic-mouse])
         (widget/process-frame
           game)))))