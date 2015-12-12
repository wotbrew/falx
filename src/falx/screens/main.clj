(ns falx.screens.main
  (:require [falx.screen :as screen]
            [falx.input :as input]
            [clj-gdx :as gdx]
            [falx.draw.world :as draw-world]
            [falx.point :as point]
            [gdx.camera :as camera]
            [falx.entity :as entity]
            [falx.world :as world]))

(defmulti get-command-action
  (fn [screen world input frame command] command))

(defmethod get-command-action :default
  [screen world input frame command]
  nil)

(defmulti get-command-actions
  (fn [screen world input frame command] command))

(defmethod get-command-actions :default
  [screen world input frame command]
  (when-some [ca (get-command-action screen world input frame command)]
    [ca]))

;; ============
;; MOVE CAM

(def cam-speed 500)

(def cam-fast-mult 2.0)

(defn cam-modifier-down?
  [input]
  (-> input :keyboard :pressed (contains? :shift-left)))

(defn get-cam-shift-amount
  [input frame]
  (* (if (cam-modifier-down? input) cam-fast-mult 1.0)
     cam-speed
     (:delta frame 0)))

(defn shift-cam-action
  [input frame direction]
  {:type  :shift-cam
   :point (point/scale
            direction
            (get-cam-shift-amount input frame))})

(defmethod get-command-action :cam-up
  [_ _ input frame _]
  (shift-cam-action input frame point/north))

(defmethod get-command-action :cam-down
  [_ _ input frame _]
  (shift-cam-action input frame point/south))

(defmethod get-command-action :cam-left
  [_ _ input frame _]
  (shift-cam-action input frame point/west))

(defmethod get-command-action :cam-right
  [_ _ input frame _]
  (shift-cam-action input frame point/east))

(defmethod screen/act :shift-cam
  [screen {:keys [point]}]
  (update-in screen [:camera :point] point/add point))

;; ==============
;; CLICKS

(defn get-mouse-point
  [screen input]
  (let [camera (:camera screen)
        mouse (:mouse input)
        mouse-point (:point mouse)
        world-point (camera/get-world-point camera mouse-point)]
    (mapv int (point/mult world-point
                          (/ 1 draw-world/cell-width)
                          (/ 1 draw-world/cell-height)))))

(defn get-mouse-cell
  [screen input]
  (let [point (get-mouse-point screen input)]
    (entity/cell (:level screen) point)))

(defmethod get-command-actions :click
  [screen world input frame _]
  (let [cell (get-mouse-cell screen input)
        entities (world/get-entities-with world :cell cell)]
    (for [e entities]
      (do
        (prn e)
        {:type   :entity-selected
         :entity e}))))

;; ==============
;; INPUT

(def bindings
  (->> [(input/bind-key-pressed :cam-up :w)
        (input/bind-key-pressed :cam-left :a)
        (input/bind-key-pressed :cam-right :d)
        (input/bind-key-pressed :cam-down :s)

        (input/bind-button-hit :click :left)]
       (group-by input/get-binding-key)))

(defmethod screen/get-input-actions :screen/main
  [screen world input frame]
  (let [commands (input/get-commands input bindings)]
    (mapcat (partial get-command-actions screen world input frame) commands)))

(def default
  {:type   :screen/main
   :camera gdx/default-camera
   :level :testing})

(defmethod screen/draw! :screen/main
  [screen world input frame]
  (gdx/using-camera
    (:camera screen gdx/default-camera)
    (draw-world/draw! world (:level screen))))