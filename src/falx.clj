(ns falx
  (:require [clj-gdx :as gdx]
            [falx.game :as g]
            [falx.frame :as frame]
            [falx.input :as input]
            [falx.world :as world]
            [clojure.tools.logging :refer [error info debug]]
            [falx.io
             [draw :as draw]
             [debug :as debug]]
            [falx.ui :as ui]
            [falx.ui
             [game :as ui-game]]
            [falx.sub
             [input :as sub-input]
             [ui :as sub-ui]]
            [falx.protocol :as p]
            [falx.position :as pos]))

(def max-fps
  60)

(def app
  (assoc gdx/default-app
    :max-background-fps max-fps
    :max-foreground-fps max-fps))

(def default-display
  (:display app))

(def default-screen-size
  (:size default-display))

(def gstate
  (agent
    (-> (g/game sub-input/subm sub-ui/subm)
        (g/add-actor {:id 0
                      :name "fred"
                      :player? true
                      :player 0})
        (world/set-pos 0 (pos/cell [4 4] :testing))
        (g/add-actor-coll
          (ui-game/get-actors (first default-screen-size) (second default-screen-size))))))

(defn push-events!
  []
  (let [p (promise)]
    (send gstate (fn [g]
                   (let [events (:events g)]
                     (deliver p events)
                     (assoc g :events []))))
    (await gstate)
    (future (run! debug/event! @p))))

(defn serve-requests!
  []
  (let [p (promise)]
    (send gstate (fn [g]
                   (let [requests (:requests g)]
                     (deliver p requests)
                     (assoc g :requests []))))
    (await gstate)
    (future (doseq [req @p]
              (send gstate g/respond req (p/-get-response req))))))

(defn update-gstate!
  [frame input]
  (send gstate g/run-subs :frame frame)
  (send gstate g/run-subs :input input)
  (push-events!)
  (serve-requests!))

(def debug-ui?
  true)

(gdx/defrender
  (try
    (when debug-ui?
      (send gstate #(g/add-actor-coll % (ui-game/get-actors (-> % :display :size (or default-screen-size) first)
                                                            (-> % :display :size (or default-screen-size) second)))))
    (let [g @gstate
          display (gdx/get-display)
          input (input/input @gdx/keyboard-state @gdx/mouse-state)
          delta (gdx/get-delta-time)
          fps (gdx/get-fps)
          frame-time (frame/frame-time delta fps)
          frame (frame/frame g display frame-time)]
      (update-gstate! frame input)
      (gdx/using-camera gdx/default-camera
        (draw/ui! g))

      (draw/string! {:fps fps
                     :time (:time g)} 0 0 800 64))
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(defn -main
  [& args]
  (info "Starting application")
  (gdx/start-app! app)
  (info "Started application"))

(comment
  (-main))