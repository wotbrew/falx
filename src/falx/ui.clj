(ns falx.ui
  (:require [falx.draw :as draw]
            [falx.rect :as rect]
            [falx.sprite :as sprite]
            [falx.util :as util]
            [falx.world :as world]
            [gdx.color :as color]
            [clj-gdx :as gdx]
            [falx.point :as point]
            [falx.input :as input]
            [gdx.camera :as camera]))

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

(defmulti draw! (fn [e frame] (:type e)))

(defmethod draw! :default
  [e frame]
  (when (:rect e)
    (draw/string! (:type e) (:rect e))))

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

(defn panel
  [coll]
  {:type :ui/panel
   :coll coll})

(defmethod draw! :ui/panel
  [e frame]
  (run! #(draw! % frame) (:coll e)))

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

(defmethod draw! :ui/sprite
  [e _]
  (draw/sprite! (:sprite e) (:rect e) (:context e)))

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

(defmethod draw! :ui/box
  [e _]
  (draw/box! (:rect e) (:context e)))

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

(defmethod draw! :ui/tiled
  [e _]
  (draw/tiled! (:sprite e) (:rect e) (:context e)))

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

(defmethod draw! :ui/string
  [e _]
  (draw/string! (:string e) (:rect e) (:context e)))

(defn button
  [s rect]
  {:type   :ui/button
   :string s
   :rect   rect})

(defmethod draw! :ui/button
  [e frame]
  (let [[x y w h] (:rect e)
        context (cond
                  (not (:enabled? e)) {:color gray}
                  (:hovering? e) {:color white}
                  :else {:color light-gray})]
    (draw/box! x y w h context)
    (draw/centered-string! (:string e) x y w h context)))

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
  (cond->
    []
    (:entered-hovering? e) (conj {:type :ui.event/button-hover-enter
                                  :button e})
    (:exited-hovering? e) (conj {:type :ui.event/button-hover-exit
                                 :button e})
    (and (:enabled? e) (:clicked? e)) (conj {:type :ui.event/button-clicked
                                             :button e})))

;; =====
;; Game Screen

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

(def cam-speed
  250)

(def cam-fast-speed
  (* cam-speed 2.5))

(defn process-camera
  [e input delta]
  (let [keyboard (:keyboard input)
        camera (:camera e)
        pressed? (:pressed keyboard #{})
        cam-fast? (pressed? :shift-left)
        cam-speed (* delta (if cam-fast? cam-fast-speed cam-speed))]
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

(defmethod get-events :ui/game-view
  [e frame]
  (let [input (:input frame)
        mouse (:mouse input)
        {:keys [camera rect]} e]
    (when (input/clicked? input rect)
      [(merge
         {:type   :ui.event/game-view-clicked
          :camera camera
          :point  (:point mouse)}
         (select-keys e
                      [:cell-size
                       :cell-width
                       :cell-height
                       :level]))])))

(defmethod draw! :ui/game-view
  [e frame]
  (let [world (:world frame)
        actors (world/get-all-actors world)
        {:keys [cell-width cell-height ]} e]
    (gdx/using-camera (:camera e gdx/default-camera)
      (doseq [a actors
              :let [point (:point a)]
              :when point
              :let [[x y] point]]
        (draw/sprite! sprite/human-male
                      (* x cell-width)
                      (* y cell-height)
                      cell-width
                      cell-height)))))

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