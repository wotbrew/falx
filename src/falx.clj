(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [falx.game :as game]
            [falx.draw :as draw]
            [falx.sprite :as sprite]
            [falx.actor :as actor]
            [falx.position :as pos]
            [falx.ui :as ui]

            [falx.game
             [click :as game-click]
             [debug :as game-debug]]))

(def max-fps 60)

(defn get-input
  []
  {:mouse @gdx/mouse-state
   :keyboard @gdx/keyboard-state})

(def game
  (do
    (when (and (bound? #'game) (some? game))
      (game/close! game))
    (-> (game/game)
        game-click/install!
        game-debug/install!)))

(gdx/defrender
  (try
    (let [frame (game/get-current-frame game)]
      (game/process-frame! game frame)
      (ui/draw! (game/get-ui game) frame)
      (draw/object! (:fps frame) 0 0 64 32)
      (draw/object! (:input frame) 0 32 800 32)
      (let [[x y] (:point (:mouse (:input frame)))]
        (draw/sprite! sprite/mouse-point x y 32 32)))
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(def app
  (assoc gdx/default-app
    :max-background-fps max-fps
    :max-foreground-fps max-fps))

(defn init!
  []
  (let [display (gdx/get-display)]
    (send (:ui-agent game) (constantly (ui/game-screen (:size display)))))
  (game/replace-actor! game {:id 0 :name "Fred"})
  (game/update-actor! game 0 actor/put (pos/cell [6 6] :foo)))

(defn -main
  [& args]
  (info "Starting application")
  (gdx/start-app! app)
  (try
    (init!)
    (catch Throwable e))
  (info "Started application"))

(comment
  (init!)
  (-main))