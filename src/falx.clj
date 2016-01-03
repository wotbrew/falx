(ns falx
  (:require [clj-gdx :as gdx]
            [falx.draw.world :as draw-world]
            [falx.input :as input]
            [falx.state :as state]
            [falx.game :as game]))

(def max-fps 60)

(defn get-action-event
  [action input]
  {:type   [:event.action action]
   :action action
   :input  input})

(defn get-input-events
  [input]
  (map #(get-action-event % input) (:actions input)))

(defn process-frame
  [game input delta]
  (let [events (get-input-events input)]
    (-> game
        (game/publish-events events)
        (assoc :input input
               :delta delta))))

(gdx/defrender
  (try
    (let [input (input/get-input-state input/temp-bindings**)
          delta (gdx/get-delta-time)
          game (state/update-game! process-frame input delta)]
      (gdx/using-camera
        (:world-camera game gdx/default-camera)
        (draw-world/draw-level!
          (:world game)
          :testing))
      (gdx/draw-string! (gdx/get-fps) 0 0 128)
      (gdx/draw-string! (input/get-mouse-point input) 0 32 512)
      (gdx/draw-string! (:actions input) 0 64 512))
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