(ns falx.input
  (:require [gdx.camera :as camera]
            [falx.draw.world :as draw-world]))

(defn key-pressed?
  [input key]
  (-> input :keyboard :pressed (contains? key)))

(def modifier-keys #{:shift-left :shift-right})

(defn modifier-pressed?
  [input]
  (some #(key-pressed? input %) modifier-keys))

(defn click?
  [input]
  (-> input :mouse :hit :left))

(defn get-mouse-point
  [input]
  (-> input :mouse :point))

;; ============
;; MOVE CAM

(def cam-up-key :w)

(def cam-left-key :a)

(def cam-right-key :d)

(def cam-down-key :s)

(def cam-speed 500)

(def cam-fast-mult 2.0)

(defn get-cam-shift-amount
  [input frame]
  (* (:delta frame 0) cam-speed (if (modifier-pressed? input)
                                  cam-fast-mult
                                  1.0)))

(defn get-cam-actions
  [input game frame]
  (filterv
    some?
    [(when (key-pressed? input cam-up-key)
       {:type  :action/shift-camera
        :point [0 (* -1 (get-cam-shift-amount input frame))]})
     (when (key-pressed? input cam-down-key)
       {:type  :action/shift-camera
        :point [0 (get-cam-shift-amount input frame)]})
     (when (key-pressed? input cam-left-key)
       {:type  :action/shift-camera
        :point [(* -1 (get-cam-shift-amount input frame)) 0]})
     (when (key-pressed? input cam-right-key)
       {:type  :action/shift-camera
        :point [(get-cam-shift-amount input frame) 0]})]))

(defn get-world-point
  [game point]
  (camera/get-world-point (:world-camera game) point))

(defn get-mouse-world-point
  [game input]
  (let [[x y] (get-mouse-point input)]
    (get-world-point game [(int (/ x draw-world/cell-width))
                           (int (/ y draw-world/cell-height))])))

(defn get-click-actions
  [input game]
  (when (click? input)
    (prn "click")
    (prn (get-mouse-world-point game input))))

(defn get-input-actions
  [input game frame]
  (into
    []
    (comp cat (filter some?))
    [(get-cam-actions input game frame)
     (get-click-actions input game)]))

