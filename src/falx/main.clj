(ns falx.main
  (:require [falx.scene :as scene]
            [falx.debug :as debug]
            [falx.ui :as ui]
            [falx.input :as input]
            [falx.keyboard :as keyboard]
            [falx.sprite :as sprite]
            [falx.gdx :as gdx]
            [falx.ui.protocols :as uiproto]
            [falx.gdx.camera :as cam]
            [falx.draw :as d]
            [falx.point :as pt]
            [falx.time :as time]
            [falx.state :as state]
            [falx.gdx.mouse :as gdx-mouse]
            [falx.db :as db]
            [falx.creature :as creature]
            [falx.entity :as entity]
            [falx.party :as party]))

(def cam
  (delay (gdx/camera [800 600])))

(state/defsignal
  ::mouse.point
  (gdx/signal
    (let [pt @gdx-mouse/point]
      (cam/world-pt @cam pt))))

(state/defsignal
  ::mouse.cell
  (gdx/signal
    (let [pt @gdx-mouse/point]
      (pt/div (cam/world-pt @cam pt) 32 32))))

(defn mouse-pos
  [gs]
  (let [slice (party/slice gs)]
    (entity/pos slice (::mouse.cell gs [0 0]))))

(defn at-mouse-pos
  [gs]
  (db/query gs ::entity/pos (mouse-pos gs)))

(def game*
  (reify uiproto/IDraw
    (-draw! [this gs rect]
      (let [[x y w h] rect
            hw (int (/ w 2))
            hh (int (/ h 2))
            [cx cy] (::camera.point gs [0 0])
            cam @cam]
        (cam/set-size! cam w h)
        (cam/set-pos! cam (+ cx hw x) (+ cy hh y))
        (gdx/with-cam
          cam
          (doseq [e (db/query gs ::entity/level :testlevel)
                  :let [pt (::entity/point e)]
                  :when pt
                  :let [[x y] pt]]
            (entity/draw! e gs (* x 32) (* y 32) 32 32)))))))

(defn game-click
  [gs rect]
  (reduce entity/click gs (at-mouse-pos gs)))

(defn game-alt-click
  [gs rect]
  (reduce entity/alt-click gs (at-mouse-pos gs)))

(def game
  (-> game*
      (ui/click game-click)
      (ui/alt-click game-alt-click)))

(def mouse
  (ui/at-mouse sprite/mouse-point 32 32))

(def scene*
  (scene/stack
    game
    (scene/fit #'debug/table 400 96)
    mouse))

(def commands*
  {::command.exit (input/hit ::keyboard/key.esc)

   ::command.cam-left (input/pressed ::keyboard/key.a)
   ::command.cam-right (input/pressed ::keyboard/key.d)
   ::command.cam-up (input/pressed ::keyboard/key.w)
   ::command.cam-down (input/pressed ::keyboard/key.s)})

(def commands
  (into {} (map (juxt key (comp input/compile val))) commands*))

(defmulti check-command (fn [gs command] command))

(defmethod check-command :default
  [gs _]
  true)

(defmulti user-command (fn [gs k] k))

(defmethod user-command :default
  [gs _]
  gs)

(def cam-speed
  1.0)

(def cam-speed-factor
  500)

(defn move-cam
  [gs direction]
  (let [mod? (input/mod? gs)
        mult (if mod? 2.0 1.0)]
    (update gs ::camera.point
            (fn [pt]
              (let [pt (or pt [0 0])
                    delta (::time/delta gs 0.0)]
                (pt/add pt (pt/scale direction
                                     (* delta
                                        cam-speed
                                        cam-speed-factor
                                        mult))))))))

(defmethod user-command ::command.exit
  [gs _]
  (assoc gs :falx/screen :falx.screen/menu))

(defmethod user-command ::command.cam-left
  [gs _]
  (move-cam gs pt/west))

(defmethod user-command ::command.cam-right
  [gs _]
  (move-cam gs pt/east))

(defmethod user-command ::command.cam-up
  [gs _]
  (move-cam gs pt/north))

(defmethod user-command ::command.cam-down
  [gs _]
  (move-cam gs pt/south))

(defn handle-user-input
  [gs]
  (reduce-kv (fn [gs k i]
               (if (and (input/check gs i)
                        (check-command gs k))
                 (user-command gs k)
                 gs)) gs commands))

(defn handle-all
  [gs rect]
  (handle-user-input gs))

(def scene
  (ui/behaviour
    scene*
    handle-all))