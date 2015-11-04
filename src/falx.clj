(ns falx
  (:require [clj-gdx :as gdx]
            [falx.ui :as ui]))

(def default-game
  {:mouse    gdx/default-mouse
   :keyboard gdx/default-keyboard
   :display gdx/default-display
   :ui-camera gdx/default-camera
   :game-camera gdx/default-camera
   :delta 0.0
   :fps 0})

(def game-state
  (atom default-game))

(defn get-next-game
  [game]
  (assoc game
         :mouse @gdx/mouse-state
         :keyboard @gdx/keyboard-state
         :display (gdx/get-display)
         :fps (gdx/get-fps)
         :delta (gdx/get-delta-time)))

(def ui-state
  (agent ui/ui))

(gdx/defrender
  (try
    (let [game (swap! game-state get-next-game)
          ui @ui-state
          input-events (future (ui/get-input-events ui game))]
      (send ui-state ui/update-widget game)
      (gdx/using-camera
        (:ui-camera game)
        (ui/draw! ui))
      (deref input-events))
    (catch Throwable e
      (println e)
      (Thread/sleep 5000))))

(def app
  gdx/default-app)

(defn main
  [& args]
  (gdx/start-app! app))
