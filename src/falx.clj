(ns falx
  (:require [clj-gdx :as gdx]
            [falx.game :as game]
            [falx.draw.world :as draw-world]
            [falx.screen :as screen]
            [falx.input :as input]
            [falx.world :as world]))

(def max-fps 60)

(def game-state (atom game/default))

(gdx/defrender
  (try
    (let [input {:keyboard @gdx/keyboard-state
                 :mouse    @gdx/mouse-state}
          frame {:delta (gdx/get-delta-time)
                 :frame-id (gdx/get-frame-id)}
          game (swap! game-state
                      (fn [{:keys [screen world] :as game}]
                        (let [actions (screen/get-input-actions screen world input frame)]
                          (game/run-actions game actions))))
          {:keys [screen world]} game]
      (screen/draw! screen world input frame)
      (gdx/using-camera
        gdx/default-camera
        (gdx/draw-string! (gdx/get-fps) 0 0 128)
        #_(gdx/draw-string! (input/get-mouse-point input) 0 16 128)
        #_(gdx/draw-string! (input/get-mouse-world-point game input) 0 32 128)))
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