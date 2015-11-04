(ns falx.ui
  (:require [falx.sprite :as sprite]
            [clj-gdx :as gdx]))

(defprotocol IWidget
  (draw! [this]))

(defprotocol IUpdateWidget
  (update-widget [this game]))

(defprotocol IInputWidget
  (get-input-events [this game]))

(extend-type Object
  IUpdateWidget
  (update-widget [this _]
    this)
  IInputWidget
  (get-input-events [this _]
    nil))

(defrecord WithUpdate [widget f]
  IWidget
  (draw! [this]
    (draw! widget))
  IUpdateWidget
  (update-widget [this game]
    (update this :widget f game))
  IInputWidget
  (get-input-events [this game]
    (get-input-events widget game)))

(defn with-update
  [widget f]
  (->WithUpdate widget f))

(defrecord Mouse [mouse sprite]
  IWidget
  (draw! [this]
    (gdx/draw! sprite (:point mouse)))
  IUpdateWidget
  (update-widget [this game]
    (assoc this :mouse (:mouse game))))

(defn mouse
  []
  (map->Mouse {:name :mouse
               :mouse gdx/default-mouse
               :sprite sprite/mouse}))

(defrecord Label [point
                  context
                  text]
  IWidget
  (draw! [this]
    (gdx/draw! (str text) point context)))

(defn label
  ([point text]
    (label point text {}))
  ([point text context]
    (map->Label {:point point
                 :text text
                 :context context})))

(defrecord TextButton [rect
                       text
                       hover-over?
                       ;;events
                       on-click-event])

(defn text-button
  [rect text & {:as opts}]
  (map->TextButton (assoc opts
                     :text text
                     :rect rect)))

(defn fps-label
  ([point]
    (fps-label point {}))
  ([point context]
    (-> (label point "" context)
        (with-update (fn [w game]
                       (assoc w :text (:fps game))))
        (assoc :name :fps-label))))

(defrecord Panel [widgets]
  IWidget
  (draw! [this]
    (run! draw! widgets))
  IInputWidget
  (get-input-events [this game]
    (mapcat #(get-input-events % game) widgets))
  IUpdateWidget
  (update-widget [this game]
    (update this :widgets
            (fn [widgets]
              (mapv #(update-widget % game) widgets)))))

(defn panel
  [& widgets]
  (->Panel (vec widgets)))

(def ui
  (panel
    (fps-label [0 0])
    (label [64 64] "foobar")
    (mouse)))
