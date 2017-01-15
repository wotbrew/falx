(ns falx.play
  (:require [falx.ui :as ui]
            [falx.game-state :as gs]
            [falx.party :as party]
            [falx.gdx :as gdx]
            [falx.frame :as frame]
            [falx.game :as g]
            [clojure.java.io :as io]
            [falx.character :as char]
            [falx.point :as pt])
  (:import (com.badlogic.gdx Input$Keys)))

(def tmpfloor
  (gdx/texture-region
    (gdx/texture (io/resource "tiles/castledungeon.png"))
    0 0 32 32))

(def tmpcorpse
  (gdx/texture-region
    char/goblins
    32 0 32 32))

(defn draw-entity!
  [gs e x y w h]
  (case (:type e)
    :party (party/draw! gs e x y w h)
    :floor (gdx/draw! tmpfloor x y w h)
    :corpse (let [[ox oy] (:offset e [0.0 0.0])
                  [cw ch] (-> gs :settings :cell-size)
                  x (+ x (* ox cw))
                  y (+ y (* oy ch))]
              (gdx/draw! tmpcorpse x y w h))
    (gdx/draw! "?" x y w h)))

(defn draw-slice!
  [gs slice]
  (let [[cw ch] (-> gs :settings :cell-size)
        cw (long cw)
        ch (long ch)]
    (doseq [eid (gs/query gs :slice slice)
            :let [e (gs/pull gs eid)
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
                         :layer :corpse})
        (draw-slice! gs {:level :test
                         :layer :creature})))))

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
          (g/update-state! (:game frame) update-in [:settings :cell-size] (comp pt/dpoint pt/div) 0.9))
        (when (pos? delta-z)
          (g/update-state! (:game frame) update-in [:settings :cell-size] pt/mul 0.9))
        (when (or (not= 0 delta-x)
                  (not= 0 delta-y))
          (g/update-state!
            (:game frame)
            update
            :camera
            pt/add
            [(* 250 delta-x mod delta)
             (* 250 delta-y mod delta)]))))))

(def move-party-handler
  (reify falx.ui.protocols/IScreenObject
    (-handle! [this frame x y w h]
      (when-not (:ai-turn? (:state frame))
        (let [delta (cond
                      (frame/key-hit? frame Input$Keys/A) [-1 0]
                      (frame/key-hit? frame Input$Keys/D) [1 0]
                      (frame/key-hit? frame Input$Keys/S) [0 1]
                      (frame/key-hit? frame Input$Keys/W) [0 -1]
                      :else [0 0])]
          (when (not= delta [0 0])
            (g/update-state! (:game frame) gs/move-active-party delta)))))))

(ui/defscene :play
  camera-handler
  move-party-handler
  (ui/if-elem (ui/key-combo-pred Input$Keys/ESCAPE)
    (ui/gs-behaviour #(ui/goto % :main-menu)))
  game-panel
  (ui/if-elem
    (ui/gs-pred :ai-turn-sequence)
    (ui/restrict-height 64
      (ui/center (ui/gs-dynamic #(format "AI TURN! (id: %s) %s to go."
                                         (peek (:ai-turn-sequence %))
                                         (count (:ai-turn-sequence %))))))))

(defmethod ui/scene-name :play [_] "Play")