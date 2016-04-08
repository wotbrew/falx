(ns falx
  (:require [clj-gdx :as gdx]
            [falx.game :as g]
            [falx.frame :as frame]
            [falx.input :as input]
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

(defn get-screen
  [g]
  (->> (ui-game/get-actors g
                           (-> g :display :size (or default-screen-size) first)
                           (-> g :display :size (or default-screen-size) second))))

(def gstate
  (agent
    (let [g (-> (g/game sub-input/subm sub-ui/subm)
                (g/add-actor {:id      0
                              :type :actor/creature
                              :name    "fred"
                              :player  0})
                (g/set-cell 0 (pos/cell [4 4] :testing))

                (g/add-actor {:id      1
                              :type :actor/creature
                              :name    "bob"
                              :player  1})
                (g/set-cell 1 (pos/cell [6 5] :testing))


                (g/add-actor {:id      2
                              :type :actor/creature
                              :name    "ethel"
                              :player  2})
                (g/set-cell 2 (pos/cell [1 2] :testing2)))]
      (g/add-actor-coll
        g
        (get-screen g)))
    :error-handler
    (fn [a exc]
      (error exc))
    :error-mode :continue))

(defn push-events!
  []
  (let [p (promise)]
    (send gstate (fn [g]
                   (let [events (:events g)]
                     (deliver p events)
                     (assoc g :events []))))
    (future (run! debug/event! @p))))

(defn serve-requests!
  []
  (let [p (promise)]
    (send gstate (fn [g]
                   (let [requests (:requests g)]
                     (deliver p requests)
                     (assoc g :requests []))))
    (future (doseq [req @p]
              (send gstate g/respond req (p/-get-response req))))))

(defn update-gstate!
  [frame input]
  (send gstate g/run-subs :input input)
  (send gstate g/run-subs :frame frame)
  (push-events!)
  (serve-requests!))

(def debug-ui?
  true)

(gdx/defrender
  (try
    (when (zero? (mod (gdx/get-frame-id) 30))
      (await gstate))
    (when (and debug-ui?
               (zero? (mod (gdx/get-frame-id) 15)))
      (send gstate #(g/add-actor-coll % (get-screen %))))
    (let [g @gstate
          display (gdx/get-display)
          input (input/input @gdx/keyboard-state @gdx/mouse-state)
          delta (gdx/get-delta-time)
          fps (gdx/get-fps)
          frame-time (frame/frame-time delta fps)
          frame (frame/frame g display frame-time)]
      (update-gstate! frame input)
      (draw/ui! g)
      (draw/string! {:fps fps} 0 0 800 64 {:font gdx/default-font}))
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

(defn set-setting!
  [k v]
  (send gstate g/set-setting k v)
  nil)

(defn get-actor
  [id]
  (g/get-actor @gstate id))
