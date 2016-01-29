(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info]]
            [falx.game :as game]))

(def max-fps 60)

(gdx/defrender
  (try
    (let [world @game/world
          mouse @gdx/mouse-state]
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
  (info "Started application"))