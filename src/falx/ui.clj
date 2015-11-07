(ns falx.ui
  (:require [falx.sprite :as sprite]
            [falx.rect :as rect]
            [falx.theme :as theme]
            [clj-gdx :as gdx]))

(defprotocol IWidget
  (draw! [this]))

(defprotocol IUpdateWidget
  (update-widget [this game]))

(defprotocol IInputWidget
  (get-input-events [this game]))

(extend-type Object
  IWidget
  (draw! [this])
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

(defrecord CenteredLabel [rect
                          text-rect
                          text
                          previous-text
                          context]
  IWidget
  (draw! [this]
    (when text-rect
      (gdx/draw-in! (str text) text-rect context)))
  IUpdateWidget
  (update-widget [this game]
     (if (= text previous-text)
       this
       (let [bounds (gdx/get-string-wrapped-bounds
                      (str text)
                      (nth rect 2)
                      (:font context gdx/default-font))]
         (assoc this
           :text-rect (rect/center-rect rect bounds)
           :previous-text text)))))

(defn centered-label
  ([rect text]
    (centered-label rect text {}))
  ([rect text context]
   (map->CenteredLabel
     {:rect    rect
      :text    text
      :context context})))

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

(defn draw-box!
  ([rect]
   (draw-box! rect {}))
  ([rect context]
   (let [[x y w h] rect
         s sprite/pixel]
     (gdx/using-texture-region-context
       context
       (gdx/-draw-in! s x y w 1 {})
       (gdx/-draw-in! s x y 1 h {})
       (gdx/-draw-in! s (+ x w) y 1 h {})
       (gdx/-draw-in! s x (+ y h) w 1 {})))))

(defrecord Box [rect context]
  IWidget
  (draw! [this]
    (draw-box! rect context)))

(defn box
  ([rect]
    (box rect {}))
  ([rect context]
    (->Box rect context)))

(defn contains-mouse?
  [rect mouse]
  (rect/contains-point? rect (:point mouse)))

(defrecord OnHoverContext [widget hover-context context]
  IWidget
  (draw! [this]
    (draw! widget))
  IUpdateWidget
  (update-widget [this game]
    (update
      this
      :widget
      #(-> (assoc % :context (if (contains-mouse? (:rect widget) (:mouse game))
                               hover-context
                               context))
           (update-widget game))))
  IInputWidget
  (get-input-events [this game]
    (get-input-events widget game)))

(defn on-hover-context
  ([widget]
   (on-hover-context widget {:color theme/yellow}))
  ([widget hover-context]
   (on-hover-context widget hover-context {}))
  ([widget hover-context context]
   (map->OnHoverContext
     {:widget        widget
      :hover-context hover-context
      :context       context})))

(defn hover-box
  ([rect]
   (hover-box rect {:color theme/yellow}))
  ([rect hover-context]
   (hover-box rect hover-context {:color theme/white}))
  ([rect hover-context context]
   (on-hover-context (box rect context) hover-context context)))

(defn text-button
  [rect text]
  (panel
    (hover-box rect)
    (on-hover-context
      (centered-label rect text))))

(def ui
  (panel
    (fps-label [0 0])
    (text-button [32 32 64 32] "foobar")
    (mouse)))

;;things sometimes scale with screen dimensions
;;positions
;;things in those positions
;;the things can change
;;the positions can change
;;some positions are hot and react to
;;hover
;;clicks
;;the things need to react to game state

(defn update-state
  [ui game]
  )

;;behaviours
(comment
  (update-state [this game])
  (get-events [this])


  :hovering?
  :enabled?
  :disabled?

  :on-hover rect fn
  :on-click rect fn
  :on-right-click rect fn)