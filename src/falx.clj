(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [falx.input :as input]
            [falx.process :as pr]
            [falx.draw :as draw]))

(def max-fps
  60)

(def app
  (assoc gdx/default-app
    :max-background-fps max-fps
    :max-foreground-fps max-fps))

(def default-game
  {})

(def game-process
  (do (when (bound? #'game-process)
        (debug "Stopping game")
        (pr/stop! game-process))
      (pr/process default-game)))

(def game-time
  (volatile! 0.0))

(gdx/defrender
  (try
    (let [mouse @gdx/mouse-state
          keyboard @gdx/keyboard-state
          delta-seconds (gdx/get-delta-time)
          time-seconds (vswap! game-time + delta-seconds)
          delta-ms (Math/floor (* 1000.0 delta-seconds))
          time-ms (Math/floor (* 1000.0 time-seconds))
          g (pr/get-state game-process)]
      (pr/actions! game-process (input/get-actions g keyboard mouse))
      (draw/string! (gdx/get-fps) 0 0 512 32))
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(defn -main
  [& args]
  (info "Starting application")
  (gdx/start-app! app)
  (info "Started application"))

(comment
  (-main))