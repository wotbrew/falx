(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info]]
            [falx.game :as game]))

(def max-fps 60)

(defn get-input
  []
  {:mouse @gdx/mouse-state
   :keyboard @gdx/keyboard-state})

(defn get-frame
  []
  {:delta (gdx/get-delta-time)
   :fps (gdx/get-fps)
   :display (gdx/get-display)
   :input (get-input)
   :world @game/world})

(gdx/defrender
  (try
    (let [frame (get-frame)])
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