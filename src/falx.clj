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

(def default-ui (menu/screen default-game {}))

(def widget-state (agent {:widget default-ui
                          :state {}}))

(def ui-state (agent default-ui))

(event/defhandler
  :event/goto-menu
  :goto-menu
  (fn [_]
    (send widget-state #(assoc % :widget (menu/screen @game-state (:state %))))))

(event/defhandler
  :event/goto-roster
  :goto-roster
  (fn [_]
    (send widget-state #(assoc % :widget (roster/screen @game-state (:state %))))))

(event/defhandler
  :event/goto-create
  :goto-create
  (fn [_]
    (send widget-state #(assoc % :widget (create/screen @game-state (:state %))))))

(def hover-text (atom {}))

(event/defhandler
  :event/set-hover-text
  :set-hover-text
  (fn [action]
    (swap! hover-text (:text action))))

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
          {:keys [widget]} @widget-state]
      (send widget-state
            (fn [{:keys [widget state]}]
              (let [w (widget/process-frame widget game state)]
                {:widget w
                 :state  (-> (widget/update-state state w)
                             (assoc :hover-text (widget/get-hover-text w game)))})))
      (future
        (publish-ui-events! widget game))
      (future
        (react-to-ui! widget game))
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
    :max-foreground-fps max-fps))

(defn -main
  [& args]
  (gdx/start-app! app))
