(ns falx
  (:require [clj-gdx :as gdx]
            [falx.game :as game]
            [falx.event :as event]
            [falx.screen :as screen]
            [falx.state :as state]))

(def max-fps 60)


(gdx/defrender
  (try
    (let [input {:keyboard @gdx/keyboard-state
                 :mouse    @gdx/mouse-state}
          frame {:delta (gdx/get-delta-time)
                 :frame-id (gdx/get-frame-id)}
          game (state/run-frame! input frame)
          {:keys [screen world]} game]
      (screen/draw! screen world input frame)
      (gdx/using-camera
        gdx/default-camera
        (gdx/draw-string! (gdx/get-fps) 0 0 128)))
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