(ns falx.widget
  (:require [falx.draw :as draw]
            [falx.rect :as rect]
            [falx.mouse :as mouse]
            [falx.keyboard :as keyboard]
            [falx.sprite :as sprite]
            [falx.theme :as theme]
            [clojure.string :as str]))


(defmulti draw-widget! :type)

(defmethod draw-widget! :default
  [m])

(defmulti update-widget* (fn [m game] (:type m)))

(defn update-widget
  [m game]
  (cond->
    m
    (:on-update m) ((:on-update m) game)
    :always (update-widget* game)))

(defmethod update-widget* :default
  [m _]
  m)

(defmulti get-input-events (fn [m game] (:type m)))

(defmethod get-input-events :default
  [_ _]
  nil)

(defn panel
  [coll]
  {:type :ui/panel
   :coll coll})

(defmethod draw-widget! :ui/panel
  [m]
  (run! draw-widget! (:coll m)))

(defmethod update-widget* :ui/panel
  [m game]
  (update m :coll (partial mapv #(update-widget % game))))

(defmethod get-input-events :ui/panel
  [m game]
  (mapcat #(get-input-events % game) (:coll m)))

(defn sprite
  [rect sprite]
  {:type :ui/sprite
   :rect rect
   :sprite sprite})

(defmethod draw-widget! :ui/sprite
  [m]
  (draw/sprite! (:sprite m) (:rect m) (:context m)))

(def basic-mouse
  (-> (sprite [0 0 32 32] sprite/mouse)
      (assoc
        :on-update
        (fn [m game]
          (assoc m :rect (let [[x y] (-> game :mouse :point)]
                           [x y 32 23]))))))

(defn label
  [rect text]
  {:type :ui/label
   :rect rect
   :text text
   :context {}})

(defmethod draw-widget! :ui/label
  [m]
  (draw/centered-string! (:rect m) (:text m) (:context m)))

(defn debug-label
  [rect f]
  (assoc (label rect "Initializing...")
    :on-update (fn [m game]
                 (assoc m :text (f game)))))

(defn fps-label
  [rect]
  (debug-label rect #(str "fps: " (:fps % 0))))

(defn box
  [rect]
  {:type :ui/box
   :rect rect
   :context {}})

(defmethod draw-widget! :ui/box
  [m]
  (draw/box! (:rect m) (:context m)))

(defn text-button
  [text rect]
  {:type :ui/text-button
   :text text
   :rect rect})

(defmethod draw-widget! :ui/text-button
  [m]
  (let [{:keys [rect text]} m
        rect (or rect rect/default)]
    (draw/text-button! m rect)))

(defmethod update-widget* :ui/text-button
  [m game]
  (cond
    (:disabled? m) m
    :else
    (assoc m :highlighted? (mouse/in? (:mouse game) (:rect m rect/default)))))

(defmethod get-input-events :ui/text-button
  [m game]
  (when (and (:highlighted? m) (mouse/clicked? (:mouse game)))
    (when-some [ie (:click-event m)]
      [ie])))

(defn filler
  [rect]
  {:type :ui/filler
   :rect rect})

(defmethod draw-widget! :ui/filler
  [m]
  (draw/tiled-sprites! sprite/blank (:rect m)))

(defn filler-border
  [rect]
  (let [[x y w h] rect]
    (panel
      [(filler [x y w 32])
       (filler [x y 32 h])
       (filler [(+ x w -32) y 32 h])
       (filler [x (+ y h -32) w 32])])))

(defn text-input-box
  [rect]
  {:type :ui/text-input
   :rect rect
   :entered-text "foobar"})

(defmethod draw-widget! :ui/text-input
  [m]
  (let [{:keys [rect]} m]
    (draw/box! rect {:color theme/gray})
    (draw/centered-string! rect (:text m ""))))

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

(defmethod update-widget* :ui/text-input
  [m game]
  (let [max-fps (:max-fps game 60)
        d (mod (:frame-id game 0) max-fps)
        text (:entered-text m "")
        keyboard (:keyboard game)
        hit (:hit keyboard)
        text' (reduce #(edit-string %1 %2 keyboard) text hit)]
    (assoc m
      :text (if (< d (/ max-fps 2)) (str text "_") (str text "  "))
      :entered-text text')))

(defmethod get-input-events :ui/text-input
  [m game]
  nil)
