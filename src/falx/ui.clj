(ns falx.ui
  (:require [falx.draw :as draw]
            [falx.rect :as rect]
            [falx.sprite :as sprite]
            [falx.util :as util]
            [falx.world :as world]))

;; ====
;; Colors


(def gray
  {:red 0.5,
   :green 0.5,
   :blue 0.5,
   :alpha 1.0,
   :float-bits -8.4903784E37})

(def light-gray
  {:red 0.75,
   :green 0.75,
   :blue 0.75,
   :alpha 1.0,
   :float-bits -1.2743907E38})

(def white
  {:red 1.0,
   :green 1.0,
   :blue 1.0,
   :alpha 1.0,
   :float-bits -1.7014117E38})

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

(defn game-screen-rect
  [width height]
  (let [vmargin game-screen-vmargin
        hmargin game-screen-hmargin]
    [vmargin
     0
     (util/floor-to-nearest (- width (* 2 vmargin)) 32)
     (util/floor-to-nearest (- height hmargin) 32)]))

(defn game-view
  [rect]
  {:type :ui/game-view
   :rect rect})

(defmethod draw! :ui/game-view
  [e frame]
  (let [world (:world frame)
        actors (world/get-all-actors world)]
    (doseq [a actors
            :let [point (:point a)]
            :when point
            :let [[x y] point]]
      (draw/sprite! sprite/human-male (* x 32) (* y 32) 32 32))))

(defn game-screen
  ([size]
   (let [[w h] size]
     (game-screen w h)))
  ([width height]
   (panel
     (let [[x y w h :as gr] (game-screen-rect width height)]
       [(game-view gr)
        (blocks [(- x 32) y 32 height])
        (blocks [(+ x w) y 32 height])
        (blocks [0 (+ y h) width 32])]))))