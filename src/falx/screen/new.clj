(ns falx.screen.new
  (:require [falx.widget :as widget]
            [falx.theme :as theme]
            [falx.rect :as rect]
            [falx.action :as action]))

(defn get-state
  [game]
  (-> game :ui :new))

(defn update-state
  [game f & args]
  (update-in game [:ui :new] #(apply f % args)))

(defn centered-rect
  [size]
  (let [[width height] size]
    (rect/center-rect [0 0 width height] [640 480])))

(defn left-rect
  [rect]
  (let [[x y w h] rect]
    [x (+ y 32) (int (/ w 2)) h]))

(defn right-rect
  [rect]
  (let [[x y w h] rect
        hw (int (/ w 2))]
    [(+ x hw) (+ y 32) hw h]))

(defn title-text
  [rect]
  (-> (widget/label rect ":: CREATE ::")
      (assoc :context {:color theme/red})))


;; =============
;; CANCEL BUTTON

(derive :new/cancel-button :ui/text-button)
(derive :new/cancel-button :ui/nav-button)

(defn cancel-button
  [rect]
  {:type :new/cancel-button
   :text "Cancel (Esc)"
   :rect rect
   :screen-key :menu})

;;=============
;; OK BUTTON

(derive :new/ok-button :ui/text-button)
(derive :new/ok-button :ui/nav-button)

(defn ok-button
  [rect]
  {:type :new/ok-button
   :text "Begin! (Enter)"
   :rect rect
   :screen-key :roster})

(defmethod widget/on-frame :new/ok-button
  [m game]
  (assoc m :disabled? (empty? (:party (get-state game)))))

;; ==============

(defn bottom-right-buttons
  [rect]
  (let [coll [ok-button
              cancel-button]
        rects (rect/fit-horizontal rect (count coll))]
    (widget/panel
      (mapv (fn [f rect] (f rect)) coll rects))))

(defn left-panel
  [game rect]
  (let [[x y w h] rect]
    (widget/panel
      [(widget/filler-border rect)])))

(defn right-panel
  [game rect]
  (let [[x y w h] rect]
    (widget/panel
      [(widget/filler-border rect)
       (bottom-right-buttons [(+ x w -288) (+ y h -64) 256 32])])))

(defn screen
  [game size]
  (let [[x y w h :as r] (centered-rect size)
        lr (left-rect r)
        rr (right-rect r)]
    (widget/panel
      [(title-text [x y w 32])
       (widget/filler-border (rect/extend r 32))
       (left-panel game lr)
       (right-panel game rr)])))

(defmethod widget/get-screen :new
  [_ game size]
  (-> (screen game size)
      (widget/process-frame game)))