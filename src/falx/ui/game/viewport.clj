(ns falx.ui.game.viewport
  (:require [falx.game :as g]
            [falx.point :as point]
            [falx.ui.game.bottom :as bottom]
            [falx.ui.game.right :as right]
            [falx.element :as e]))

(defn update-camera
  ([g f]
   (g/update-attr g ::panel :elements update-in [:viewport :camera] f))
  ([g f & args]
   (update-camera g #(apply f % args))))

(defn get-camera
  [g]
  (-> g (g/get-attr ::panel :elements) :viewport :camera))

(defn resolve-camera
  [g w h]
  (let [old-camera (get-camera g)
        [ow oh] (:size old-camera)]
    (if (and (= ow w) (= oh h))
      old-camera
      {:type    :camera/orthographic,
       :point   [400.0 300.0],
       :size    [w h],
       :flip-y? true})))

(defn get-panel
  [g sw sh]
  (let [[x1 y1 w1 h1] (right/get-rect sw sh)
        [x2 y2 w2 h2] (bottom/get-rect sw sh)]
    {:id ::panel
     :type ::panel
     :elements {:viewport (e/viewport (resolve-camera g sw sh) [0 0 sw sh])}
     :ui-root? true
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
    (update-camera g update :point point/add x y)))

(defmethod g/uhandle [::panel [:event/key-pressed :w]]
  [g _ _]
  (move-camera g 0 -1))

(defmethod g/uhandle [::panel [:event/key-pressed :a]]
  [g _ _]
  (move-camera g -1 0))

(defmethod g/uhandle [::panel [:event/key-pressed :s]]
  [g _ _]
  (move-camera g 0 1))

(defmethod g/uhandle [::panel [:event/key-pressed :d]]
  [g _ _]
  (move-camera g 1 0))

(defn get-actors
  [g sw sh]
  [(get-panel g sw sh)])