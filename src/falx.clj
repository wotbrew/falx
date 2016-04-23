(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [falx.process :as pr]
            [falx.impl
             [camera]
             [input]
             [mouse]
             [movement]
             [selection]
             [time]]
            [falx.draw :as draw]
            [falx.time :as time]
            [falx.world :as world]
            [falx.schedule :as sched]
            [falx.render.world :as render-world]
            [falx.render.ui :as render-ui]))

(def max-fps
  60)

(def app
  (assoc gdx/default-app
    :max-background-fps max-fps
    :max-foreground-fps max-fps))

(def default-game
  {:context :game
   :screen :game

   :ui {:viewport {:camera {:point [0 0]
                            :size [800 600]}
                   :level :limbo}
        :mouse {:point [0 0]}}

   :time (time/time)
   :visual-schedule (sched/schedule)
   :sim-schedule (sched/schedule)
   :world (world/world
            {:fred {:name "Fred"
                    :cell {:level :limbo
                           :point [4 4]}}
             :bob {:name "Bob"
                   :cell {:level :limbo
                          :point [6 6]}}})})

(def game-process
  (do (when (bound? #'game-process)
        (debug "Stopping game")
        (pr/stop! game-process))
      (pr/process default-game)))

(def game-state
  (:state game-process))

(def game-time
  (volatile! 0.0))

(gdx/defrender
  (try
    (let [mouse @gdx/mouse-state
          keyboard @gdx/keyboard-state
          delta-seconds (gdx/get-delta-time)
          time-seconds (vswap! game-time + delta-seconds)
          delta-ms (long (Math/floor (* 1000.0 delta-seconds)))
          time-ms (long (Math/floor (* 1000.0 time-seconds)))
          g (pr/get-state game-process)]
      (pr/actions! game-process [{:type :handle-input
                                  :mouse mouse
                                  :keyboard keyboard}
                                 {:type :pass-time
                                  :ms delta-ms}])
      (render-ui/screen! g :game 0 0 1024 768)
      (draw/string! (gdx/get-fps) 0 0 512 32)
      (draw/string! (-> g :ui :mouse) 0 16 512 32))
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