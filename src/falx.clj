(ns falx
  (:require [clj-gdx :as gdx]
            [falx.draw.world :as draw-world]
            [falx.input :as input]
            [falx.state :as state]))

(def max-fps 60)

(gdx/defrender
  (try
    (let [game (state/get-game)]
      (draw-world/draw-level!
        (:world game)
        :testing)
      (gdx/draw-string! (gdx/get-fps) 0 0 128)
      (gdx/draw-string! (input/get-mouse-point (input/get-input-state game)) 0 32 512)
      (gdx/draw-string! (:actions (input/get-input-state game)) 0 64 512))
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