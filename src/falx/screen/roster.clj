(ns falx.screen.roster
  (:require [falx.rect :as rect]
            [falx.widget :as widget]
            [falx.theme :as theme]
            [falx.race :as race]
            [falx.action :as action]))

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

(action/defreaction
  ::kill-character
  :kill-character
  (fn [m {:keys [entity]}]
    (-> (update m :roster #(vec (remove #{entity} %)))
        (update-state dissoc :selected))))

(defmethod widget/get-click-action :roster/kill-button
  [m game]
  (when-some [entity (:selected (get-state game))]
    {:type ::kill-character
     :entity entity}))

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

;; ===============
;; CHARACTER BUTTON

(derive :roster/character-button :ui/sprite-button)

(defmethod widget/on-frame :roster/character-button
  [m game]
  (assoc m :selected? (= (:entity m) (:selected (get-state game)))))

(defmethod widget/get-click-action :roster/character-button
  [m game]
  {:type ::set-character
   :entity (:entity m)})

(action/defreaction
  ::set-character
  :set-character
  (fn [m {:keys [entity]}]
    (update-state m assoc :selected entity)))

(defn character-button
  [rect entity]
  {:type :roster/character-button
   :sprite (race/get-body-sprite (:race entity) (:gender entity))
   :entity entity
   :rect rect})

;; ==============
;; CHARACTER PANEL

(derive :roster/character-panel :ui/panel)

(defn get-character-buttons
  [rect roster]
  (for [[rect entity] (map vector (rect/fit-tiled rect [64 64]) roster)]
    (character-button rect entity)))

(defmethod widget/on-frame :roster/character-panel
  [m game]
  (let [roster (:roster game)
        last-roster (:roster m)]
    (if (= roster last-roster)
      m
      (assoc m
        :coll (get-character-buttons (:rect m) roster)
        :roster roster))))

(defn character-panel
  [rect roster]
  {:type :roster/character-panel
   :rect rect
   :coll (get-character-buttons rect roster)})

;; ============
;; SCREEN

(defn screen
  [game size]
  (let [[width height] size
        [x y w h :as r] (centered-rect size)]
    (-> (widget/panel
          [(widget/filler-border (rect/extend r 32))
           (title-text [x y w 32])
           (buttons [x (+ y 32) w 32])
           (character-panel [x (+ y 64) w 64] (:roster game))
           widget/hover-text
           widget/basic-mouse])
        (widget/process-frame
          game))))

(defmethod widget/get-screen :roster
  [_ game size]
  (screen game size))