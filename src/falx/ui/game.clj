(ns falx.ui.game
  (:require [falx.ui.game.right :as right]
            [falx.ui.game.bottom :as bottom]
            [falx.game :as g]
            [falx.point :as point]))

(defn viewport-camera
  [w h]
  {:type :camera/orthographic,
   :point [400.0 300.0],
   :size [w h],
   :flip-y? true})

(defn get-viewport
  [sw sh]
  (let [[x1 y1 w1 h1] (right/get-rect sw sh)
        [x2 y2 w2 h2] (bottom/get-rect sw sh)]
    {:id       ::viewport
     :type     :actor/viewport
     :camera   (viewport-camera sw sh)
     :ui-root? true
     :rect     [0 0 sw sh]
     ;;handles
     [:handles? [:event/key-pressed :w]] true
     [:handles? [:event/key-pressed :a]] true
     [:handles? [:event/key-pressed :s]] true
     [:handles? [:event/key-pressed :d]] true}))

(def camera-speed
  100)

(defn camera-fast?
  [g]
  (-> g :input :keyboard :pressed (contains? :shift-left)))

(defn move-camera
  [g x y]
  (let [delta (:delta g 0.0)
        mod (if (camera-fast? g)
              2.5
              1.0)
        x (* delta x camera-speed mod)
        y (* delta y camera-speed mod)]
    (g/update-attr g ::viewport :camera update :point point/add x y)))

(defmethod g/uhandle [::viewport [:event/key-pressed :w]]
  [g _ _]
  (move-camera g 0 -1))

(defmethod g/uhandle [::viewport [:event/key-pressed :a]]
  [g _ _]
  (move-camera g -1 0))

(defmethod g/uhandle [::viewport [:event/key-pressed :s]]
  [g _ _]
  (move-camera g 0 1))

(defmethod g/uhandle [::viewport [:event/key-pressed :d]]
  [g _ _]
  (move-camera g 1 0))

(defn get-actors
  [g sw sh]
  (concat
    (right/get-actors g sw sh)
    (bottom/get-actors g sw sh)
    [(get-viewport sw sh)]))
