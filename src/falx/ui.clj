(ns falx.ui
  (:require [falx.rect :as rect]
            [falx.sprite :as sprite]
            [falx.util :as util]
            [gdx.color :as color]
            [falx.point :as point]
            [falx.input :as input]
            [gdx.camera :as camera]
            [falx.position :as pos]
            [falx.world :as world]
            [falx.actor :as actor]
            [falx.request :as request]
            [falx.event :as event]
            [falx.goal :as goal]))

;; ====
;; Selection

(defn get-selected-actors
  [world]
  (world/query-actors world ::selected? true))

(defn selected-actor?
  [actor]
  (::selected? actor))

(defn selectable-actor?
  [actor]
  (actor/creature? actor))

(defn can-select-actor?
  [actor]
  (and (selectable-actor? actor)
       (not (selected-actor? actor))))

(defn select-actor
  [actor]
  (if (can-select-actor? actor)
    (as-> actor x
          (assoc x ::selected? true)
          (actor/publish x (event/actor-selected x)))
    actor))

(defn can-unselect-actor?
  [actor]
  (selected-actor? actor))

(defn unselect-actor
  [actor]
  (if (can-unselect-actor? actor)
    (as-> actor x
          (dissoc x ::selected?)
          (actor/publish x (event/actor-unselected x)))
    actor))

(defn toggle-actor-selection
  [actor]
  (if (selected-actor? actor)
    (unselect-actor actor)
    (select-actor actor)))

(defn select-only-actor
  [world actor]
  (->
    (reduce #(world/update-actor %1 (:id %2) unselect-actor) world
            (get-selected-actors world))
    (world/update-actor (:id actor) select-actor)))

;; ====
;; Game View Clicks

(defn get-actor-click-messages
  [actor]
  (concat
    (when (selectable-actor? actor)
      [(request/select-only-actor actor)])))

(defn get-world-click-messages
  [world cell]
  (when-not (world/solid-at? world cell)
    (for [a (get-selected-actors world)
          :when (not= cell (:cell a))]
      (request/give-goal a (goal/move cell)))))

;; ====
;; Colors

(def gray
  (color/color 0.5 0.5 0.5 1))

(def light-gray
  (color/color 0.75 0.75 0.75 1))

(def white
  (color/color 1 1 1 1))

(def black
  (color/color 0 0 0 1))

(def yellow
  (color/color 1 1 0 1))

(def green
  (color/color 0 1 0 1))

(def red
  (color/color 1 0 0 1))

;; ====
;; Widgets

(defmulti process (fn [e frame state] (:type e)))

(defmethod process :default
  [e frame state]
  e)

(defmulti get-events (fn [e frame] (:type e)))

(defmethod get-events :default
  [e frame]
  nil)

(defmulti next-state (fn [state e] (:type e)))

(defmethod next-state :default
  [state e]
  state)

(defmulti enabled? (fn [e frame] (:type e)))

(defmethod enabled? :default
  [e frame]
  true)

;; Widgets - Definitions

(defn panel
  [coll]
  {:type :ui/panel
   :coll coll})

(defmethod process :ui/panel
  [e frame state]
  (update e :coll (partial mapv #(process % frame state))))

(defmethod get-events :ui/panel
  [e frame]
  (mapcat #(get-events % frame) (:coll e)))

(defmethod next-state :ui/panel
  [state e]
  (reduce next-state state (:coll e)))

(defn sprite
  ([sprite rect]
   {:type   :ui/sprite
    :sprite sprite
    :rect   rect})
  ([sprite rect context]
   {:type :ui/sprite
    :sprite sprite
    :rect rect
    :context context}))

(defn pixel
  ([rect]
   (pixel rect {}))
  ([rect context]
   (sprite sprite/pixel rect context)))

(defn background
  ([rect]
   (pixel rect {:color black})))

(defn box
  ([rect]
   {:type :ui/box
    :rect rect})
  ([rect context]
   {:type :ui/box
    :rect rect
    :context context}))

(defn tiled
  ([sprite rect]
   {:type :ui/tiled
    :sprite sprite
    :rect rect})
  ([sprite rect context]
   {:type :ui/tiled
    :sprite sprite
    :rect rect
    :context context}))

(defn blocks
  [rect]
  (tiled sprite/block rect))

(defn string
  ([s rect]
   {:type :ui/string
    :string s
    :rect rect})
  ([s rect context]
   {:type :ui/string
    :string s
    :rect rect
    :context context}))

(defn button
  [s rect]
  {:type   :ui/button
   :string s
   :rect   rect})

(defn- process-enabled-button
  [e frame state]
  (let [input (:input frame)
        mouse (:mouse input)
        mouse-point (:point mouse)
        was-hovering? (:hovering? e)
        hovering? (rect/contains-point? (:rect e) mouse-point)
        clicked? (and hovering? (contains? (:hit mouse) :left))]
    (assoc e :hovering? hovering?
             :entered-hovering? (and (not was-hovering?) hovering?)
             :exited-hovering? (and was-hovering? (not hovering?))
             :clicked? clicked?)))

(defmethod process :ui/button
  [e frame state]
  (if (not (enabled? e frame))
    (dissoc e :enabled?)
    (let [e (assoc e :enabled? true)]
      (process-enabled-button e frame state))))

(defmethod get-events :ui/button
  [e frame]
  (let [point (-> frame :input :mouse :point)]
    (cond->
      []
      (:entered-hovering? e) (conj (event/ui-hover-enter e point))
      (:exited-hovering? e) (conj (event/ui-hover-exit e point))
      (and (:enabled? e) (:clicked? e)) (conj (event/ui-clicked e :left point)))))

;; =====
;; Widgets - Game Screen

(def game-screen-vmargin
  (* 5 32))

(def game-screen-hmargin
  (* 7 32))

(defn game-view-rect
  [width height]
  (let [vmargin game-screen-vmargin
        hmargin game-screen-hmargin]
    [vmargin
     0
     (util/floor-to-nearest (- width (* 2 vmargin)) 32)
     (util/floor-to-nearest (- height hmargin) 32)]))

(defn game-view
  [rect sw sh]
  {:type   :ui/game-view
   :camera {:type :camera/orthographic,
            :point [400.0 300.0],
            :size [sw sh],
            :flip-y? true}
   :cell-size [32 32]
   :cell-width 32
   :cell-height 32
   :level "testing-level"
   :rect   rect})

(def ^:dynamic *cam-speed*
  250)

(def ^:dynamic *cam-fast-speed*
  (* *cam-speed* 2.5))

(defn process-camera
  [e input delta]
  (let [keyboard (:keyboard input)
        camera (:camera e)
        pressed? (:pressed keyboard #{})
        cam-fast? (pressed? :shift-left)
        cam-speed (* delta (if cam-fast? *cam-fast-speed* *cam-fast-speed*))]
    (assoc e
      :camera
      (cond->
        camera
        (pressed? :w) (update :point point/add 0 (- cam-speed))
        (pressed? :a) (update :point point/add (- cam-speed) 0)
        (pressed? :d) (update :point point/add cam-speed 0)
        (pressed? :s) (update :point point/add 0 cam-speed)))))

(defmethod process :ui/game-view
  [e frame state]
  (let [{:keys [input delta]} frame]
    (cond->
      (-> (process-camera e input delta)))))

(defn get-world-cell
  [game-view point]
  (let [{:keys [camera level cell-width cell-height]} game-view
        [x y] (camera/get-world-point camera point)]
    (pos/cell [(int (/ x cell-width))
               (int (/ y cell-height))]
              level)))


(defn get-game-view-click-events
  [game-view world input button point]
  (let [cell (get-world-cell game-view point)]
    [(event/ui-clicked game-view button point)
     (event/world-clicked cell input button)
     (event/multi (map #(event/actor-clicked % input button) (world/get-at world cell)))]))

(defmethod get-events :ui/game-view
  [e frame]
  (let [input (:input frame)
        point (:point (:mouse input))
        rect (:rect e)
        button (input/some-click input rect)]
    (cond->
      []
      ;;
      button
      (into (get-game-view-click-events e (:world frame) input button point)))))

(defn game-left-rect
  [width height]
  (let [[x y w h] (game-view-rect width height)]
    [0 0 (- x 32) h]))

(defn game-left
  [width height]
  (pixel (game-left-rect width height)))

(defn game-right-rect
  [width height]
  (let [[x y w h] (game-view-rect width height)]
    [(+ x w 32) 0 (- width w x 32) h]))

(defn game-right
  [width height]
  (pixel (game-right-rect width height) {:color green}))

(defn game-bottom-left-rect
  [width height]
  (let [[x y w h] (game-view-rect width height)]
    [0 (+ y h 32) (- x 32) (- height h y 32)]))

(defn game-bottom-left
  [width height]
  (pixel (game-bottom-left-rect width height) {:color red}))

(defn game-bottom-right-rect
  [width height]
  (let [[x y w h] (game-view-rect width height)]
    [(+ x w 32) (+ y h 32) (- x 32) (- height h y 32)]))

(defn game-bottom-right
  [width height]
  (pixel (game-bottom-right-rect width height) {:color yellow}))

(defn game-bottom-rect
  [width height]
  (let [[x y w h] (game-view-rect width height)]
    [x (+ y h) w (- height y h)]))

(defn game-bottom
  [width height]
  (pixel (game-bottom-rect width height) {:color gray}))

(defn game-screen
  ([size]
   (let [[w h] size]
     (game-screen w h)))
  ([width height]
   (panel
     (let [[x y w h :as gr] (game-view-rect width height)]
       [(game-view gr width height)
        (game-left width height)
        (game-right width height)
        (game-bottom-left width height)
        (game-bottom-right width height)
        (game-bottom width height)

        (blocks [(- x 32) y 32 height])
        (blocks [(+ x w) y 32 height])
        (blocks [0 (+ y h) width 32])]))))