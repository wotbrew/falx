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

;; =============
;; MALE BUTTON

(derive :create/male-button :ui/text-button)

(defmethod widget/on-frame :create/male-button
  [m game st]
  (assoc m :selected? (-> st ::gender (= :male))))

(defmethod widget/update-state :create/male-button
  [st m]
  (if (:clicked? m)
    (assoc st ::gender :male)
    st))

(defn male-button
  [rect]
  {:type :create/male-button
   :text "M"
   :rect rect})

;; =============
;; FEMALE BUTTON

(derive :create/female-button :ui/text-button)

(defmethod widget/on-frame :create/female-button
  [m game st]
  (assoc m :selected? (-> st ::gender (not= :male))))

(defmethod widget/update-state :create/female-button
  [st m]
  (if (:clicked? m)
    (assoc st ::gender :female)
    st))

(defn female-button
  [rect]
  {:type :create/female-button
   :text "F"
   :rect rect})

;; ================
;; GENDER ROW

(defn gender-label
  [rect]
  (widget/label rect "Gender:"))

(defn gender-row
  [rect]
  (let [[x y w h] rect]
    (widget/panel
      [(gender-label [x y 64 h])
       (male-button [(+ x 16 64) y 64 32])
       (female-button [(+ x 1 16 64 64) y 64 32])])))

;; ============
;; RACE BUTTON

(derive :create/race-button :ui/sprite-button)

(defmethod widget/on-frame :create/race-button
  [m game st]
  (assoc m :sprite (race/get-body-sprite (:race m) (::gender st))
           :selected? (-> st ::race (or race/human) (= (:race m)))))

(defmethod widget/update-state :create/race-button
  [st m]
  (if (:clicked? m)
    (assoc st ::race (:race m))
    st))

(defn race-button
  [rect race]
  {:type :create/race-button
   :race race
   :sprite (race/get-body-sprite race {})
   :rect rect})

;; =============
;; RACE ROW

(defn race-row
  [rect]
  (let [[x y w h] rect]
    (widget/panel
      (cons (widget/label [x y 64 h] "Race:")
            (for [[i race] (map-indexed vector race/all)]
              (race-button [(+ x 64 (* i 48)) y 48 48] race))))))


(defn screen
  ([game st]
   (screen game st (-> game :ui-camera :size)))
  ([game st size]
   (let [[x y w h :as r] (centered-rect size)]
     (-> (widget/panel
           [(widget/filler-border (rect/extend r 32))
            (title-text [x y w 32])
            (name-row [x (+ y 32) (+ 64 256) 32])
            (gender-row [(+ x 96 256) (+ y 32) (+ 64 256) 32])
            (race-row [x (+ y 64 16) w 48])
            (bottom-right-buttons [(+ x w -256) (+ y h -32) 256 32])
            widget/basic-mouse])
         (widget/process-frame
           game
           st)))))