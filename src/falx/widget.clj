(ns falx.widget
  (:require [falx.draw :as draw]
            [falx.rect :as rect]
            [falx.mouse :as mouse]
            [falx.keyboard :as keyboard]
            [falx.sprite :as sprite]
            [falx.theme :as theme]
            [clojure.string :as str]
            [clj-gdx :as gdx]))


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
  ([coll x]
   (if (some? x)
     (conj coll x)
     coll))
  ([coll x & xs]
    (reduce mcond (mcond coll x) xs)))

;; ================================
;; INPUT EVENTS

(defmulti get-input-events (fn [m game] (:type m)))

(defmulti get-click-event (fn [m game] (:type m)))

(defmethod get-click-event :default
  [_ _])

(defmulti get-hover-event (fn [m game] (:type m)))

(defmethod get-hover-event :default
  [m game])

(defmethod get-input-events :default
  [m game]
  (cond->
    []
    ;;
    (:clicked? m) (mcond (get-click-event m game))
    (:hovering? m) (mcond (get-hover-event m game))))

;; ==========================
;; INPUT ACTIONS

(defmulti get-input-actions (fn [m game] (:type m)))

(defmulti get-click-action (fn [m game] (:type m)))

(defmethod get-click-action :default
  [m game])

(defmulti get-hover-action (fn [m game] (:type m)))

(defmethod get-hover-action :default
  [m game])

(defmethod get-input-actions :default
  [m game]
  (cond->
    []
    (:hovering? m) (mcond (get-hover-action m game))
    (:clicked? m) (mcond (get-click-action m game))))

;; ==========================

(defmulti get-hover-text (fn [m game] (:type m)))

(defmethod get-hover-text :default
  [m game])

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

(defmethod get-hover-text :ui/panel
  [m game]
  (some #(get-hover-text % game) (:coll m)))

;;=======================
;; SPRITE

(defn sprite
  [rect sprite]
  {:type :ui/sprite
   :rect rect
   :sprite sprite})

;; ======================
;; SPRITE BUTTON

(defn sprite-button
  [rect sprite]
  {:type :ui/sprite-button
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
;; HOVER TEXT

(def hover-text
  {:type :ui/hover-text
   :rect [0 0 32 32]})

(defmethod on-frame :ui/hover-text
  [m game]
  (let [[x y] (-> game :mouse :point)
        x (+ 16 x)
        y (+ 16 y)
        text (:hover-text (:ui game))]
    (if (some? text)
      (let [[w h] (gdx/get-string-wrapped-bounds text 160)]
        (assoc m :rect [x y w h]
                 :text text))
      (dissoc m :text))))

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
