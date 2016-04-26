(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [falx.process :as pr]
            [falx.impl
             [click]
             [input]
             [mouse]
             [movement]
             [selection]
             [time]
             [viewport]]
            [falx.draw :as draw]
            [falx.time :as time]
            [falx.world :as world]
            [falx.schedule :as sched]
            [falx.render.ui :as render-ui]
            [falx.screen :as screen]
            [falx.action :as action]))

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
                   :level :limbo
                   :cell-size [32 32]}
        :mouse {:point [0 0]
                :cell {:point [0 0]
                       :level :limbo}}}

   :time (time/time)
   :visual-schedule (sched/schedule)
   :sim-schedule (sched/schedule)

   :player {:party [:fred :bob]
            :selected #{}
            :controlled #{:fred :bob}}

   :world (world/world
            (into {:fred {:name "Fred"
                           :type :creature
                           :layer :creature
                           :solid? true
                           :cell {:level :limbo
                                  :point [4 4]}}
                    :bob {:name "Bob"
                          :type :creature
                          :layer :creature
                          :solid? true
                          :cell {:level :limbo
                                 :point [6 6]}}}
                   (for [x (range 0 32)
                         y (range 0 32)]
                     [[:floor x y] {:name "Floor"
                                    :type :terrain
                                    :layer :floor
                                    :cell {:level :limbo
                                           :point [x y]}}])))})

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
      (pr/actions! game-process [(action/handle-input keyboard mouse)
                                 (action/move-mouse (:point mouse))
                                 (action/pass-time delta-ms)])
      (render-ui/draw! g (screen/screen (:screen g) 1024 768))
      (draw/string! (gdx/get-fps) 0 0 512 32)
      (draw/string! (-> g :ai) 0 16 512 32)
      (draw/string! (-> g :player) 0 32 512 32))
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