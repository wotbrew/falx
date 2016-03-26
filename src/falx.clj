(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [falx.game :as game]
            [falx.io.draw :as draw]
            [falx.sprite :as sprite]
            [falx.actor :as actor]
            [falx.position :as pos]
            [falx.ui :as ui]

            [falx.flow
             [ai :as flow-ai]
             [ui :as flow-ui]
             [debug :as flow-debug]]))

(def max-fps 60)

(defn init!
  [game]
  (let [display (gdx/get-display)]
    (reset! (:ui-atom game) (ui/game-screen (:size display))))
  (game/replace-actor! game {:id 0
                             :name "Fred"
                             :solid? true
                             :type :actor.type/creature
                             :layer :layer.type/creature})
  (game/update-actor! game 0 actor/put (pos/cell [6 6] "testing-level")))

(def game
  (do
    (when (and (bound? #'game) (some? game))
      (game/close! game))
    (doto (game/game)
      flow-debug/install!
      flow-ui/install!
      flow-ai/install!
      (game/publish! {:type :falx.event/game-started})
      init!
      (game/publish! {:type :falx.event/game-initialized}))))

(gdx/defrender
  (try
    (let [frame (game/get-current-frame game)]
      (game/process-frame! game frame)
      (draw/widget! (game/get-ui game) frame)
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

(defn -main
  [& args]
  (info "Starting application")
  (gdx/start-app! app)
  (info "Started application"))

(comment
  (init! game)
  (-main))