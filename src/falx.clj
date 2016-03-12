(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info]]
            [falx.game :as game]
            [falx.draw :as draw]
            [falx.sprite :as sprite]))

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
    (let [frame (get-frame)]
      (draw/object! (:fps frame) 0 0 64 32)
      (draw/object! (:input frame) 0 32 800 32)
      (let [[x y] (:point (:mouse (:input frame)))]
        (draw/tiled! sprite/human-male x y 64 64)))
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