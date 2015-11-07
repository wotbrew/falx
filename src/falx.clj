(ns falx
  (:require [clj-gdx :as gdx]
            [falx.graphics :as graphics]))

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

(def ui
  (atom {:type :ui/text-button
          :text "foo"}))

(gdx/defrender
  (try
    (let [game (swap! game-state get-next-game)]
      (swap! ui falx.screen/update-text-button game [32 32 64 32])
      (gdx/using-camera
        (:ui-camera game)
        (graphics/draw-in! @ui [32 32 64 32])))
    (catch Throwable e
      (println e)
      (Thread/sleep 5000))))

(def app
  gdx/default-app)

(defn -main
  [& args]
  (gdx/start-app! app))
