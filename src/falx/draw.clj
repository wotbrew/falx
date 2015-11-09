(ns falx.draw
  (:require [clj-gdx :as gdx]
            [falx.sprite :as sprite]
            [falx.theme :as theme]
            [falx.rect :as rect]))

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
   (box! rect {}))
  ([rect context]
   (let [[x y w h] rect
         s sprite/pixel]
     (gdx/using-sprite-options
       context
       (sprite! s x y w 1)
       (sprite! s x y 1 h)
       (sprite! s (+ x w) y 1 h)
       (sprite! s x (+ y h) w 1)))))

;; ===================================
;; MAPS

(defmulti thing! (fn [m rect] (:type m)))

(defmethod thing! :default
  [m rect])

(defmulti disabled! (fn [m rect] (:type m)))

(defmethod disabled! :default
  [m rect]
  (thing! m rect))

(defmulti highlighted! (fn [m rect] (:type m)))

(defmethod highlighted! :default
  [m rect]
  (thing! m rect))

(defmulti selected! (fn [m rect] (:type m)))

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
      (:highlighted? m) (highlighted! m rect)
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
   (box! rect box-context)
   (centered-string! rect text text-context)))

(defmethod thing! :ui/text-button
  [m rect]
  (text-button! rect (:text m "")))

(defmethod highlighted! :ui/text-button
  [{:keys [text]} rect]
  (text-button! rect (str "- " text " -") {:color theme/white}))

(defmethod selected! :ui/text-button
  [{:keys [text]} rect]
  (text-button! rect (str "- " text " -") {:color theme/white}))

(defmethod disabled! :ui/text-button
  [{:keys [text]} rect]
  (text-button! rect text {:color theme/dark-gray}))

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

