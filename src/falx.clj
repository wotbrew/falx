(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info]]
            [falx.game :as game]))

(def max-fps 60)

(def game
  (agent (game/game [])))

(defn render-game!
  [db]
  (gdx/draw-string! "empty" 0 0 96))

(gdx/defrender
  (try
    (let [db {}]
      (render-game! db)
      ;;poll input in background, send changes to game for next frame
      )
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(def app
  (assoc gdx/default-app
    :max-background-fps max-fps
    :max-foreground-fps max-fps))

(defn -main
  [& args]
  (info "Starting application")
  (gdx/start-app! app)
  (info "Started application")
  #_(init/init!))