(ns falx.play
  (:require [falx.ui :as ui]
            [falx.game-state :as gs]
            [falx.party :as party]
            [falx.gdx :as gdx]
            [falx.frame :as frame]
            [falx.game :as g]
            [clojure.java.io :as io])
  (:import (com.badlogic.gdx Input$Keys)))

(def tmpfloor
  (gdx/texture-region
    (gdx/texture (io/resource "tiles/castledungeon.png"))
    0 0 32 32))

(defn draw-entity!
  [gs e x y w h]
  (case (:type e)
    :party (party/draw! gs e x y w h)
    :floor (gdx/draw! tmpfloor x y w h)))

(defn draw-slice!
  [gs slice]
  (let [[cw ch] (gs/setting gs :cell-size [32 32])
        cw (long cw)
        ch (long ch)]
    (doseq [eid (gs/q gs :slice slice)
            :let [e (gs/entity gs eid)
                  [x y] (:pt e)]
            :when (some? x)]
      (draw-entity! gs e (* x cw) (* y ch) cw ch))))

(def game-panel
  (reify falx.ui.protocols/IScreenObject
    (-handle! [this {gs :state :as frame} x y w h]
      (gdx/with-translation
        (:camera gs [0 0])
        (draw-slice! gs {:level :test
                         :layer :floor})
        (draw-slice! gs {:level :test
                         :layer :creature})))))

(defn point-camera-at
  ([gs pt]
   (let [[x y] pt]
     (point-camera-at gs x y)))
  ([gs x y]
   (let [[cw ch] (gs/setting gs :cell-size [32 32])
         [sw sh] (gs/setting gs :resolution [640 480])]
     (assoc gs :camera [(double (+ (/ sw 2) (* x (- cw))))
                        (double (+ (/ sh 2) (* y (- ch))))]))))

(defn player-party-id
  [gs]
  (first (gs/q gs :player-party? true)))

(defn player-party
  [gs]
  (gs/entity gs (player-party-id gs)))

(defn point-camera-at-party
  ([gs]
   (if-some [pt (:pt (player-party gs))]
     (point-camera-at gs pt)
     gs)))

(def camera-handler
  (reify falx.ui.protocols/IScreenObject
    (-handle! [this frame x y w h]
      (let [delta-x (cond->
                      0.0
                      (frame/key-down? frame Input$Keys/LEFT) (+ 1.0)
                      (frame/key-down? frame Input$Keys/RIGHT) (+ -1.0))
            delta-y (cond->
                      0.0
                      (frame/key-down? frame Input$Keys/UP) (+ 1.0)
                      (frame/key-down? frame Input$Keys/DOWN) (+ -1.0))
            mod (if (or (frame/key-down? frame Input$Keys/SHIFT_LEFT)
                        (frame/key-down? frame Input$Keys/SHIFT_RIGHT))
                  2.5
                  1.0)
            delta-z (:scroll-delta (:tick frame) 0)
            delta (:delta (:tick frame))]
        (when (neg? delta-z)
          (g/update-state! (:game frame)
                           update-in [:settings :cell-size]
                           (fnil
                             (partial mapv (comp double /))
                             [32 32])
                           [0.9 0.9]))
        (when (pos? delta-z)
          (g/update-state! (:game frame)
                           update-in [:settings :cell-size]
                           (fnil
                             (partial mapv (comp double *))
                             [32 32])
                           [0.9 0.9]))
        (when (or (not= 0 delta-x)
                  (not= 0 delta-y))
          (g/update-state! (:game frame)
                           update :camera
                           (fnil (partial mapv +) [0 0])
                           [(* 250 delta-x mod delta)
                            (* 250 delta-y mod delta)]))))))

(defn move-party
  [gs id cell]
  (let [{:keys [level pt]} cell]
    (gs/modify
      gs id
      assoc
      :cell cell
      :level level
      :pt pt
      :layer :creature
      :slice {:layer :creature
              :level level})))

(defn move-player-party
  [gs direction]
  (if (= direction [0 0])
    gs
    (if-some [e (player-party gs)]
      (if-some [{:keys [level pt]} (:cell e)]
        (-> (move-party gs (:id e) {:level level
                                    :pt    (mapv + pt direction)})
            point-camera-at-party)
        gs)
      gs)))

(def move-party-handler
  (reify falx.ui.protocols/IScreenObject
    (-handle! [this frame x y w h]
      (when-some [pid (first (gs/q (:state frame)
                                   :player-party? true))]
        (let [delta (cond
                      (frame/key-hit? frame Input$Keys/A) [-1 0]
                      (frame/key-hit? frame Input$Keys/D) [1 0]
                      (frame/key-hit? frame Input$Keys/S) [0 1]
                      (frame/key-hit? frame Input$Keys/W) [0 -1]
                      :else [0 0])
              {:keys [level pt]} (:cell (gs/entity (:state frame) pid))
              npt (mapv + pt delta)]
          (when (not= delta [0 0])
            (g/update-state! (:game frame) move-player-party delta)))))))

(ui/defscene :play
  camera-handler
  move-party-handler
  (ui/if-elem (ui/key-combo-pred Input$Keys/ESCAPE)
    (ui/gs-behaviour #(ui/goto % :main-menu)))
  game-panel)

(defmethod ui/scene-name :play [_] "Play")