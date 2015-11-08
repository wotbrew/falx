(ns falx.screen.create
  (:require [falx.widget :as widget]
            [falx.theme :as theme]
            [falx.rect :as rect]
            [falx.sprite :as sprite]))

(defn centered-rect
  [size]
  (let [[width height] size]
    (rect/center-rect [0 0 width height] [640 480])))

(defn title-text
  [rect]
  (-> (widget/label rect ":: CREATE ::")
      (assoc :context {:color theme/red})))

(defn cancel-button
  [rect]
  (-> (widget/text-button "(C)ancel" rect)
      (assoc :click-event {:type :event/goto-roster})))

(defn ok-button
  [rect]
  (widget/text-button "(O)k" rect))

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

(defn male-button
  [rect]
  (-> (widget/text-button "M" rect)
      (assoc
        :on-update
        (fn [m game]
          (assoc m :selected? (= (::gender game :male) :male))))))

(defn female-button
  [rect]
  (-> (widget/text-button "F" rect)
      (assoc
        :on-update
        (fn [m game]
          (assoc m :selected? (= (::gender game :male) :female))))))

(defn gender-row
  [rect]
  (let [[x y w h] rect]
    (widget/panel
      [(gender-label [x y 64 h])
       (male-button [(+ x 16 64) y 64 32])
       (female-button [(+ x 1 16 64 64) y 64 32])])))

(defn screen
  ([game]
   (screen game (-> game :ui-camera :size)))
  ([game size]
   (let [[x y w h :as r] (centered-rect size)]
     (-> (widget/panel
           [(widget/fps-label [0 0 64 32])
            (widget/filler-border (rect/extend r 32))
            (title-text [x y w 32])
            (name-row [x (+ y 32) (+ 64 256) 32])
            (gender-row [(+ x 96 256) (+ y 32) (+ 64 256) 32])
            (bottom-right-buttons [(+ x w -192) (+ y h -32) 192 32])
            widget/basic-mouse])
         (widget/update-widget
           game)))))