(ns falx
  (:require [clj-gdx :as gdx]
            [falx.screen.menu :as menu]
            [falx.widget :as widget]
            [falx.event :as event]
            [falx.action :as action]
            [falx.screen
             [menu]
             [roster :as roster]
             [create :as create]]
            [falx.draw :as draw]))

(def max-fps 60)

(def default-game
  {:mouse    gdx/default-mouse
   :keyboard gdx/default-keyboard
   :display gdx/default-display
   :ui-camera gdx/default-camera
   :game-camera gdx/default-camera
   :delta 0.0
   :fps 0
   :max-fps max-fps})

(def game-state
  (atom default-game))

(defn get-next-game
  [game]
  (assoc game
    :mouse @gdx/mouse-state
    :keyboard @gdx/keyboard-state
    :display (gdx/get-display)
    :fps (gdx/get-fps)
    :frame-id (gdx/get-frame-id)
    :delta (gdx/get-delta-time)))

(def default-ui (menu/screen default-game))

(def ui-state (agent default-ui))

(event/defhandler
  :event/goto-menu
  :goto-menu
  (fn [_]
    (send ui-state (constantly (menu/screen @game-state)))))

(event/defhandler
  :event/goto-roster
  :goto-roster
  (fn [_]
    (send ui-state (constantly (roster/screen @game-state)))))

(event/defhandler
  :event/goto-create
  :goto-create
  (fn [_]
    (send ui-state (constantly (create/screen @game-state)))))

(defn publish-ui-events!
  [ui game]
  (run! event/publish! (widget/get-input-events ui game)))

(defn react-to-ui!
  [ui game]
  (let [actions (widget/get-input-actions ui game)]
    (swap! game-state #(reduce action/react % actions))))

(gdx/defrender
  (try
    (await ui-state)
    (let [game (swap! game-state get-next-game)
          ui @ui-state]
      (send ui-state widget/process-frame game)
      (future
        (publish-ui-events! ui game))
      (future
        (react-to-ui! ui game))
      (gdx/using-camera
        (:ui-camera game)
        (draw/widget!
         ui)
        (draw/string! (:fps game) [0 0 64 32])))
    (catch Throwable e
      (println e)
      (Thread/sleep 5000))))

(def app
  (assoc gdx/default-app
    :max-foreground-fps max-fps))

(defn -main
  [& args]
  (gdx/start-app! app))
