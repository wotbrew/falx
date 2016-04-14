(ns falx.ui.game.viewport
  (:require [falx.game :as g]
            [falx.point :as point]
            [falx.ui.game.bottom :as bottom]
            [falx.ui.game.right :as right]
            [falx.element :as e]
            [falx.protocol :as p]
            [gdx.camera :as camera]
            [falx.event :as event]
            [falx.position :as pos]))

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
        [x2 y2 w2 h2] (bottom/get-rect sw sh)
        bounds [0 0 (- sw w1) (- sh h2)]]
    {:id ::panel
     :type ::panel
     :elements {:viewport (e/viewport (resolve-camera g sw sh)
                                      bounds)}
     :ui-root? true
     :ui-rect bounds
     ;;handles
     [:handles? [:event/key-pressed :w]] true
     [:handles? [:event/key-pressed :a]] true
     [:handles? [:event/key-pressed :s]] true
     [:handles? [:event/key-pressed :d]] true

     [:handles? [:event/actor-clicked ::panel]] true
     [:handles? [:event/actor-clicked :actor/creature]] true
     [:handles? [:event/actor-clicked :actor/terrain]] true}))

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

(defn get-world-click-events
  [g point]
  (let [level-point (point/idiv point (g/get-cell-size g))
        cell (pos/cell level-point (g/get-selected-level g))]
    (concat
     [(event/world-clicked point)
      (event/cell-clicked point)]
     (map event/actor-clicked (g/get-at g cell)))))

(defmethod g/handle [::panel [:event/actor-clicked ::panel]]
  [g actor _]
  (let [camera (get-camera g)]
    (g/request g (reify p/IRequest
                   (-get-response [this]
                     (camera/get-world-point camera (g/get-mouse-point g)))
                   (-respond [this g point]
                     (->> (get-world-click-events g point)
                          (reduce g/publish g)))))))

(defmethod g/handle [::panel [:event/actor-clicked :actor/creature]]
  [g _ {:keys [actor]}]
  (g/select-only g (:id actor)))

(defmethod g/handle [::panel [:event/actor-clicked :actor/terrain]]
  [g _ {:keys [actor]}]
  (if (:solid? actor)
    g
    (let [cell (:cell actor)
          selected (g/get-selected g)]
      ;;do stuff
      g)))

(defn get-actors
  [g sw sh]
  [(get-panel g sw sh)])
