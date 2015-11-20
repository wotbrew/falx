(ns falx
  (:require [clj-gdx :as gdx]
            [falx.screen.menu :as menu]
            [falx.widget :as widget]
            [falx.event :as event]
            [falx.action :as action]
            [falx.screen
             [menu]
             [roster]
             [create]
             [new]
             [play]]
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

(def default-ui (falx.screen.play/screen default-game (:size (:ui-camera @game-state))))

(def widget-state (agent default-ui))

(event/defhandler
  :event/goto
  :goto
  (fn [event]
    (let [game @game-state
          widget (widget/get-screen (:screen-key event) game (:size (:ui-camera game)))]
      (send widget-state (constantly widget)))))

(defn publish-ui-events!
  [ui game]
  (run! event/publish! (widget/get-input-events ui game)))

(defn react-to-ui!
  [ui game]
  (let [actions (widget/get-input-actions ui game)]
    (swap! game-state #(reduce action/react % actions))))

(gdx/defrender
  (try
    (await widget-state)
    (let [game (swap! game-state get-next-game)
          widget @widget-state]
      (send widget-state widget/process-frame game)
      (react-to-ui! widget game)
      (publish-ui-events! widget game)
      (swap! game-state assoc-in [:ui :hover-text] (widget/get-hover-text widget game))
      (gdx/using-camera
        (:ui-camera game)
        (draw/widget!
          widget)
        (draw/string! (:fps game) [0 0 64 32])))
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