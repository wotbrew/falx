(ns falx.draw
  (:require [clj-gdx :as gdx]
            [falx.sprite :as sprite]
            [falx.theme :as theme]
            [falx.rect :as rect])
  (:import (clojure.lang IPersistentVector)))

;; ================================
;; STRINGS

(def string! gdx/draw-string!)

(defn centered-string!
  ([rect text]
   (centered-string! rect text {}))
  ([rect text context]
   (let [s (str text)
         bounds (gdx/get-string-wrapped-bounds s (nth rect 2))
         text-rect (rect/center-rect rect bounds)]
     (string! s text-rect context))))

;; ===============================
;; SPRITES

(def sprite! gdx/draw-sprite!)

(defn tiled-sprites!
  ([sprite rect]
   (tiled-sprites! sprite rect [32 32]))
  ([sprite rect size]
   (tiled-sprites! sprite rect size {}))
  ([sprite rect size context]
   (let [[x y w h] rect
         [w2 h2] size
         i (int (/ w w2))
         j (int (/ h h2))]
     (loop [i_ 0
            j_ 0]
       (if (< i_ i)
         (do
           (sprite! sprite
                         (+ x (* i_ w2))
                         (+ y (* j_ h2))
                         w2 h2 context)
           (recur (inc i_) j_))
         (when (< j_ (dec j))
           (recur 0 (inc j_))))))))

(defn box!
  ([rect]
   (box! rect {} {} {} {}))
  ([rect context]
   (box! rect context context context context))
  ([rect tcontext lcontext rcontext bcontext]
   (let [[x y w h] rect
         s sprite/pixel
         tthickness (:thickness tcontext 1)
         lthickness (:thickness lcontext 1)
         rthickness (:thickness rcontext 1)
         bthickness (:thickness bcontext 1)]
     (sprite! s x y w tthickness tcontext)
     (sprite! s x y lthickness h lcontext)
     (sprite! s (+ x w (- rthickness)) y rthickness h rcontext)
     (sprite! s x (+ y h (- bthickness)) w bthickness bcontext))))

;; ===================================
;; MAPS

(defmulti thing! (fn [o rect] (or (:type o) (class o))))

(defmethod thing! :default
  [m rect])

(defmethod thing! IPersistentVector
  [v rect]
  (run! #(thing! % rect) v))

(defmulti disabled! (fn [o rect]  (or (:type o) (class o))))

(defmethod disabled! :default
  [m rect]
  (thing! m rect))

(defmulti hovering! (fn [o rect] (or (:type o) (class o))))

(defmethod hovering! :default
  [m rect]
  (thing! m rect))

(defmulti selected! (fn [o rect] (or (:type o) (class o))))

(defmethod selected! :default
  [m rect]
  (thing! m rect))

;; =====================================
;; WIDGETS

(defn widget!
  ([m]
    (widget! m (:rect m )))
  ([m rect]
    (cond
      (:disabled? m) (disabled! m rect)
      (:hovering? m) (hovering! m rect)
      (:selected? m) (selected! m rect)
      :else (thing! m rect))))

;; ==================================
;; TEXT BUTTON

(defn text-button!
  ([rect text]
   (text-button! rect text {:color theme/light-gray}))
  ([rect text context]
   (text-button! rect text context context))
  ([rect text box-context text-context]
   (let [color (:color box-context)
         shaded {:color (theme/mult color theme/dark-gray)}]
     (box! rect box-context box-context shaded shaded))
   (centered-string! rect text text-context)))

(defmethod thing! :ui/text-button
  [m rect]
  (text-button! rect (:text m "") {:color theme/light-gray
                                   :thickness 2}))

(defmethod hovering! :ui/text-button
  [{:keys [text]} rect]
  (text-button! rect (str "- " text " -") {:color theme/white
                                           :thickness 2}))

(defmethod selected! :ui/text-button
  [{:keys [text]} rect]
  (text-button! rect (str "- " text " -") {:color theme/green
                                           :thickness 2}))

(defmethod disabled! :ui/text-button
  [{:keys [text]} rect]
  (text-button! rect text {:color theme/dark-gray
                           :thickness 2}))

;; ==================================
;; PANEL

(defmethod thing! :ui/panel
  [m rect]
  (run! widget! (:coll m)))

;; ==================================
;; LABEL

(defmethod thing! :ui/label
  [m rect]
  (centered-string! rect (:text m "") (:context m)))

;; ==================================
;; BOX

(defmethod thing! :ui/box
  [m rect]
  (box! rect (:context m)))

;; ==================================
;; FILLER

(defmethod thing! :ui/filler
  [m rect]
  (tiled-sprites! sprite/blank rect [32 32]))

;; =================================
;; TEXT INPUT

(defmethod thing! :ui/text-input
  [m rect]
  (let [{:keys [rect]} m]
    (box! rect {:color theme/gray})
    (centered-string! rect (:text m ""))))

;; ================================
;; SPRITE

(defmethod thing! :ui/sprite
  [m rect]
  (sprite! (:sprite m) rect (:context m)))

;; ==============================
;; SPRITE BUTTON

(defmethod thing! :ui/sprite-button
  [m rect]
  (sprite! (:sprite m) rect {:color theme/gray}))

(defmethod hovering! :ui/sprite-button
  [m rect]
  (when (:selected? m)
    (sprite! sprite/circle rect {:color theme/green}))
  (sprite! (:sprite m) rect {:color theme/white}))

(defmethod disabled! :ui/sprite-button
  [m rect]
  (sprite! (:sprite m) rect {:color theme/gray}))

(defmethod selected! :ui/sprite-button
  [m rect]
  (sprite! sprite/circle rect {:color theme/green})
  (sprite! (:sprite m) rect {:color theme/white}))