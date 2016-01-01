(ns falx
  (:require [clj-gdx :as gdx]
            [falx.draw.world :as draw-world]
            [falx.state :as state]))

(def max-fps 60)

(gdx/defrender
  (try
    (draw-world/draw-level!
      (:world @state/game)
      :testing)
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