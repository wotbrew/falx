(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info]]
            [falx.game :as game]
            [falx.draw :as draw]
            [falx.sprite :as sprite]
            [falx.actor :as actor]
            [falx.position :as pos]
            [falx.ui :as ui]))

(def max-fps 60)

(defn get-input
  []
  {:mouse @gdx/mouse-state
   :keyboard @gdx/keyboard-state})

(defn get-frame
  []
  {:delta (gdx/get-delta-time)
   :fps (gdx/get-fps)
   :display (gdx/get-display)
   :input (get-input)
   :world @game/world})

(gdx/defrender
  (try
    (let [frame (get-frame)
          ui (-> (ui/game-screen (:size (:display frame)))
                 (ui/process frame {}))]
      (ui/draw! ui frame)
      (game/publish-coll! (ui/get-events ui frame))
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
  (game/replace-actor! {:id 0
                        :name "Fred"})
  (game/update-actor! 0 actor/put (pos/cell [3 3] :foo)))

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