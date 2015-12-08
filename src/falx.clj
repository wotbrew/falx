(ns falx
  (:require [clj-gdx :as gdx]
            [falx.game :as game]
            [falx.input :as input]

            [falx.draw.world :as draw-world]
            [falx.world :as world]
            [falx.levels.testing :as level-testing]
            [falx.entity :as entity]))

(def max-fps 60)

(def game-atom (atom game/default))

(gdx/defrender
  (try
    (let [input {:keyboard @gdx/keyboard-state
                 :mouse    @gdx/mouse-state}
          frame {:delta (gdx/get-delta-time)
                 :fps (gdx/get-fps)
                 :frame-id (gdx/get-frame-id)}
          actions (input/get-input-actions input @game-atom frame)
          game (swap! game-atom #(reduce game/act % actions))
          world (:world game)
          world-camera (:world-camera game)]
      (gdx/using-camera
        world-camera
        (draw-world/draw! world level-testing/level))
      (gdx/using-camera
        gdx/default-camera
        (gdx/draw-string! (gdx/get-fps) 0 0 128)
        (gdx/draw-string! (input/get-mouse-world-point game input) 0 16 128)))
    (catch Throwable e
      (println e)
      (Thread/sleep 5000))))

(def app
  (assoc gdx/default-app
    :max-background-fps max-fps
    :max-foreground-fps max-fps))

(defn -main
  [& args]
  (gdx/start-app! app))