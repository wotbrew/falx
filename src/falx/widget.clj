(ns falx.widget
  (:require [falx.draw :as draw]
            [falx.rect :as rect]
            [falx.mouse :as mouse]
            [falx.keyboard :as keyboard]
            [falx.sprite :as sprite]
            [falx.theme :as theme]
            [clojure.string :as str]))

(defmulti on-hover-enter (fn [m game] (:type m)))

(defmethod on-hover-enter :default
  [m game]
  m)

(defmulti on-hover-exit (fn [m game] (:type m)))

(defmethod on-hover-exit :default
  [m game]
  m)

(defmulti on-click (fn [m game] (:type m)))

(defmethod on-click :default
  [m game]
  m)

(defmulti on-frame (fn [m game] (:type m)))

(defmethod on-frame :default
  [m game]
  m)

(defmulti process-frame (fn [m game] (:type m)))

(defmethod process-frame :default
  [m game]
  (let [mouse-in? (mouse/in? (:mouse game) (:rect m rect/default))
        clicked? (and mouse-in? (mouse/clicked? (:mouse game)))]
    (cond->
      m
      ;;
      (and mouse-in? (not (:hovering? m)))
      (-> (assoc :hovering? true)
          (on-hover-enter game))
      ;;
      (and (not mouse-in?) (:hovering? m))
      (-> (dissoc :hovering?)
          (on-hover-exit game))
      ;;
      clicked?
      (-> (assoc :clicked? true)
          (on-click game))
      ;;
      (and (not clicked?) (:clicked? m))
      (dissoc :clicked?)

      :always
      (on-frame game))))

(defn- mcond
  [coll x]
  (if (some? x)
    (conj coll x)
    coll))

;; ================================
;; INPUT EVENTS

(defmulti get-input-events (fn [m game] (:type m)))

(defmulti get-click-event (fn [m game] (:type m)))

(defmethod get-click-event :default
  [_ _])

(defmethod get-input-events :default
  [m game]
  (cond->
    []
    ;;
    (:clicked? m) (mcond (get-click-event m game))))

;; ==========================
;; INPUT ACTIONS

(defmulti get-input-actions (fn [m game] (:type m)))

(defmulti get-click-action (fn [m game] (:type m)))

(defmethod get-click-action :default
  [m game])

(defmethod get-input-actions :default
  [m game]
  (cond->
    []
    (:clicked? m) (mcond (get-click-action m game))))

;; ===========================
;; PANEL

(defn panel
  [coll]
  {:type :ui/panel
   :coll coll})

(defmethod process-frame :ui/panel
  [m game]
  (update m :coll (partial mapv #(process-frame % game))))

(defmethod get-input-events :ui/panel
  [m game]
  (mapcat #(get-input-events % game) (:coll m)))

(defmethod get-input-actions :ui/panel
  [m game]
  (mapcat #(get-input-actions % game) (:coll m)))

;;=======================
;; SPRITE

(defn sprite
  [rect sprite]
  {:type :ui/sprite
   :rect rect
   :sprite sprite})

;; ======================
;; BASIC MOUSE

(derive :ui/mouse :ui/sprite)

(defmethod process-frame :ui/mouse
  [m game]
  (assoc m :rect (mouse/rect (:mouse game))))

(def basic-mouse
  {:type :ui/mouse
   :rect rect/default
   :sprite sprite/mouse})

;; ================
;; LABEL

(defn label
  [rect text]
  {:type :ui/label
   :rect rect
   :text text
   :context {}})

;; ===============
;; BOX

(defn box
  [rect]
  {:type :ui/box
   :rect rect
   :context {}})

;; ================
;; BUTTON

(defn text-button
  [text rect]
  {:type :ui/text-button
   :text text
   :rect rect})

;; =================
;; FILLER

(defn filler
  [rect]
  {:type :ui/filler
   :rect rect})

(defn filler-border
  [rect]
  (let [[x y w h] rect]
    (panel
      [(filler [x y w 32])
       (filler [x y 32 h])
       (filler [(+ x w -32) y 32 h])
       (filler [x (+ y h -32) w 32])])))

;; =================
;; TEXT INPUT

(defn text-input-box
  [rect]
  {:type :ui/text-input
   :rect rect
   :entered-text ""})

(defn edit-string [])

(defmulti edit-string (fn [s k kboard] k))

(defmethod edit-string :default
  [s k kboard]
  (if-some [char (keyboard/key->string k)]
    (str s (if (keyboard/shift-pressed? kboard)
             (str/upper-case char)
             char))
    s))

(defmethod edit-string :space
  [s k kboard]
  (str s " "))

(defmethod edit-string :backspace
  [s k kboard]
  (apply str (butlast s)))

(defmethod edit-string :forward-del
  [s k kboard]
  (apply str (butlast s)))

(defmethod on-frame :ui/text-input
  [m game]
  (let [current-delta (:delta m 0)
        delta (:delta game 0)
        text (:entered-text m "")
        keyboard (:keyboard game)
        hit (:hit keyboard)
        text' (reduce #(edit-string %1 %2 keyboard) text hit)]
    (assoc m
      :delta (if (< current-delta 1) (+ current-delta delta) 0)
      :text (if (< current-delta 0.5) (str text "_") (str text "  "))
      :entered-text text')))

;;focus
;;focused?
;;click-event
;;on-focused-key-hit [k keyboard]
;;on-focus [k