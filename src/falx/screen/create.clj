(ns falx.screen.create
  (:require [falx.widget :as widget]
            [falx.theme :as theme]
            [falx.rect :as rect]
            [falx.sprite :as sprite]
            [falx.race :as race]
            [falx.action :as action]
            [falx.gender :as gender]))

(defn get-state
  [game]
  (-> game :ui :create))

(defn update-state
  [game f & args]
  (update-in game [:ui :create] #(apply f % args)))

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
(derive :create/cancel-button :ui/nav-button)

(defn cancel-button
  [rect]
  {:type :create/cancel-button
   :text "Cancel (Esc)"
   :rect rect
   :screen-key :roster})

;;=============
;; OK BUTTON

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

;; ============
;; GENDER BUTTON

(defmethod widget/get-click-action :create/gender-button
  [m game]
  {:type ::set-gender
   :gender (:gender m)})

(action/defreaction
  ::set-gender
  :set-gender
  (fn [m {:keys [gender]}]
    (update-state m assoc :gender gender)))

(defmethod widget/on-frame :create/gender-button
  [m game]
  (let [gender (:gender m)
        selected-gender (:gender (get-state game) gender/male)]
    (assoc m :selected? (= gender selected-gender))))

(defmethod widget/get-hover-text :create/gender-button
  [m game]
  (when (:hovering? m)
    (format
      "%s
    ----------
    Has no impact on character attributes"
      (:name (:gender m)))))

;; =============
;; MALE BUTTON

(derive :create/male-button :ui/text-button)
(derive :create/male-button :create/gender-button)

(defn male-button
  [rect]
  {:type :create/male-button
   :gender gender/male
   :text "M"
   :rect rect})

;; =============
;; FEMALE BUTTON

(derive :create/female-button :ui/text-button)
(derive :create/female-button :create/gender-button)

(defn female-button
  [rect]
  {:type :create/female-button
   :gender gender/female
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
  [m game]
  (let [{:keys [gender race]} (get-state game)
        race (or race race/human)
        widget-race (:race m)]
    (assoc m :sprite (race/get-body-sprite widget-race gender)
             :selected? (= race widget-race))))

(defmethod widget/get-click-action :create/race-button
  [m game]
  {:type ::set-race
   :race (:race m)})

(action/defreaction
  ::set-race
  :set-race
  (fn [m {:keys [race]}]
    (update-state m assoc :race race)))

(defn get-race-hover-text
  [race]
  (str (:name race "???")
       "\n-------------\n"
       (:description race "???")))

(defmethod widget/get-hover-text :create/race-button
  [m game]
  (when (:hovering? m)
    (get-race-hover-text (:race m))))

(defn race-button
  [rect race]
  {:type :create/race-button
   :race race
   :sprite (race/get-body-sprite race {})
   :rect rect})

;; ===========
;; RACE SCROLL LEFT

(derive :create/race-scroll-left :ui/text-button)

(defn race-scroll-left
  [rect]
  {:type :create/race-scroll-left
   :text "<"
   :rect rect})

;; ===========
;; RACE SCROLL RIGHT

(derive :create/race-scroll-right :ui/text-button)

(defn race-scroll-right
  [rect]
  {:type :create/race-scroll-right
   :text ">"
   :rect rect})

;; =============
;; RACE ROW

(defn race-row
  [rect]
  (let [[x y w h] rect]
    (widget/panel
      (into [(widget/label [x y 64 h] "Race:")
             (race-scroll-left [(+ x 64 16) y 36 32])
             (race-scroll-right [(+ x 96 32 (* 5 32)) y 36 32])]
            (for [[i race] (map-indexed vector (take 4 race/all))]
              (race-button [(+ x 96 32 (* i 48)) y 32 32] race))))))

(defn screen
  [game size]
  (let [[x y w h :as r] (centered-rect size)]
    (-> (widget/panel
          [(widget/filler-border (rect/extend r 32))
           (title-text [x y w 32])
           (name-row [x (+ y 32) (+ 64 256) 32])
           (gender-row [(+ x 96 256) (+ y 32) (+ 64 256) 32])
           (race-row [x (+ y 64 16) w 32])
           (bottom-right-buttons [(+ x w -256) (+ y h -32) 256 32])
           widget/hover-text
           widget/basic-mouse])
        (widget/process-frame
          game))))

(defmethod widget/get-screen :create
  [_ game size]
  (screen game size))