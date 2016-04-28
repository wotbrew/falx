(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [falx.draw :as draw]
            [falx.db :as db]
            [falx.sprite :as sprite]
            [falx.point :as point]
            [falx.game :as g]
            [falx.action :as action]
            [falx.move :as move]
            [falx.turn :as turn]
            [falx.look :as look]
            [gdx.color :as color]
            [falx.rect :as rect]))

(def max-fps
  60)

(def app
  (assoc gdx/default-app
    :max-background-fps max-fps
    :max-foreground-fps max-fps))

(def game-time
  (volatile! 0.0))

(def game
  (volatile!
    (-> (g/game
          (into
            [{:id :player
              :type :party
              :layer :creature
              :creatures [{:name "Fred"}
                          {:name "Bob"}
                          {:name "Ethel"}
                          {:name "Jim"}
                          {:name "Barbara"}
                          {:name "Long Ass Named Thing"}]
              :point [1 1]}
             {:id [:goblin 0]
              :type :party
              :layer :creature
              :creatures [{:name "Goblin"}
                          {:name "Goblin"}]
              :point [3 4]}
             {:id [:goblin 1]
              :type :party
              :layer :creature
              :creatures [{:name "Goblin"}
                          {:name "Goblin"}
                          {:name "Goblin"}
                          {:name "Goblin"}
                          {:name "Goblin"}
                          {:name "Goblin"}]
              :point [3 1]}
             {:id [:goblin 2]
              :type :party
              :layer :creature
              :creatures [{:name "Goblin"}]
              :point [5 5]}
             {:id [:goblin 3]
              :type :party
              :layer :creature
              :creatures [{:name "Goblin"}
                          {:name "Goblin"}
                          {:name "Goblin"}]
              :point [7 4]}]
            (concat
              (for [x (range 32)
                    y (range 32)]
                {:id [:floor x y]
                 :type :terrain
                 :layer :floor
                 :point [x y]})
              (for [point (concat (rect/get-edge-points [0 0 32 32])
                                  (point/get-a*-path (constantly true) [5 5] [8 8]))]
                {:id [:wall point]
                 :type :terrain
                 :layer :wall
                 :solid? true
                 :point point}))))
        (action/run [:begin-turn :player])
        (g/schedule-in [:begin-turn [:goblin 0]] 1)
        (g/schedule-in [:begin-turn [:goblin 1]] 2)
        (g/schedule-in [:begin-turn [:goblin 2]] 3)
        (g/schedule-in [:begin-turn [:goblin 3]] 4))))

(defn get-player-input-action
  [keyboard]
  (let [hit? (:hit keyboard #{})]
    (cond
      (hit? :numpad-7) [:move-player :nw]
      (hit? :numpad-8) [:move-player :n]
      (hit? :numpad-9) [:move-player :ne]
      (hit? :numpad-4) [:move-player :w]
      (hit? :numpad-5) [:end-turn :party]
      (hit? :numpad-6) [:move-player :e]
      (hit? :numpad-1) [:move-player :sw]
      (hit? :numpad-2) [:move-player :s]
      (hit? :numpad-3) [:move-player :se]

      (hit? :num-1) [:select-creature 0]
      (hit? :num-2) [:select-creature 1]
      (hit? :num-3) [:select-creature 2]
      (hit? :num-4) [:select-creature 3]
      (hit? :num-5) [:select-creature 4]
      (hit? :num-6) [:select-creature 5]

      (hit? :l) [:begin-look])))

(defn get-cursor-keyboard-action
  [keyboard]
  (let [hit? (:hit keyboard #{})]
    (cond
      (hit? :numpad-7) [:move-cursor :nw]
      (hit? :numpad-8) [:move-cursor :n]
      (hit? :numpad-9) [:move-cursor :ne]
      (hit? :numpad-4) [:move-cursor :w]
      (hit? :numpad-6) [:move-cursor :e]
      (hit? :numpad-1) [:move-cursor :sw]
      (hit? :numpad-2) [:move-cursor :s]
      (hit? :numpad-3) [:move-cursor :se]

      (hit? :n) [:move-next-cursor]
      (hit? :b) [:move-back-cursor]
      (hit? :escape) [:cancel-cursor])))

(defn tick
  [g keyboard]
  (cond
    (g/player-active? g) (action/run g (get-player-input-action keyboard))
    (g/active? g :cursor) (action/run g (get-cursor-keyboard-action keyboard))
    (g/active? g) (turn/end g (:active g))
    :else (if-some [[action tick] (g/peek-action g)]
            (if (<= (:tick game 0) tick)
              (-> (g/pop-action (assoc g :tick tick))
                  (action/run action)
                  (recur keyboard))
              (update g :tick (fnil inc 0)))
            g)))

(defn draw-log!
  [g]
  (when-some [s (peek (:log g))]
    (draw/centered-string! s 0 0 800 32)))

(def csize
  64)

(gdx/defrender
  (try
    (let [mouse @gdx/mouse-state
          keyboard @gdx/keyboard-state
          delta-seconds (gdx/get-delta-time)
          time-seconds (vswap! game-time + delta-seconds)
          delta-ms (long (Math/floor (* 1000.0 delta-seconds)))
          time-ms (long (Math/floor (* 1000.0 time-seconds)))
          g (vswap! game tick keyboard)
          party (g/get-player g)
          seed (rand-int Integer/MAX_VALUE)
          cam-point (if (g/active? g :cursor)
                      (:point (:cursor g) [0 0])
                      (:point party [0 0]))]
      ;;world
      (gdx/using-camera
        (assoc gdx/default-camera :point (point/scale cam-point csize))
        (doseq [layer [:floor
                       :wall
                       :creature]
                thing (g/get-at-layer g layer)
                :let [[x y] (:point thing)]
                :when (and x y)]
          (case (:type thing)
               :party (if (= (:id thing) :player)
                        (do (draw/sprite! sprite/human-male (* x csize) (* y csize) csize csize)
                            (draw/string! (count (:creatures thing)) (* x csize) (* y csize) csize csize))
                        (do (draw/sprite! sprite/goblin-worker (* x csize) (* y csize) csize csize)
                            (draw/string! (count (:creatures thing)) (* x csize) (* y csize) csize csize)))
               :terrain (case (:layer thing)
                          :wall (draw/sprite! sprite/castle-wall (* x csize) (* y csize) csize csize)
                          (draw/sprite! sprite/castle-floor (* x csize) (* y csize) csize csize))))

        ;; cursor
        (when-some [[x y] (:point (:cursor g))]
          (draw/box! (* csize x) (* csize y) csize csize)))
      ;;creature panels
      (loop [creatures (:creatures party)
             i 0]
        (when (< i (count creatures))
          (let [creature (nth creatures i)]
            (let [[x y w h] [(+ 32) (+ 32 (* i 72)) 64 64]]
              (draw/sprite! sprite/pixel x y w h {:color color/black})
              (draw/box! x y w h {:color (if (g/selected? g i)
                                           color/green
                                           color/light-gray)})
              (draw/sprite! sprite/human-male (+ x 16) (+ y 8) 32 32)
              (draw/string! (inc i) (+ x 3) (+ y 3) w h)))
          (recur creatures (inc i))))
      ;; selected panel
      (when-some [creature (g/get-selected g)]
        (let [[x y w h] [(+ 32) (- 600 128) 352 112]]
          (draw/sprite! sprite/pixel x y w h {:color color/black})
          (draw/box! x y w h)
          (draw/centered-string! (:name creature) (+ x 3) (+ y 3) w 24)))
      ;; focused panel
      (when-some [party (g/get-entity g (:focus g))]
        (let [[x y w h] [(+ 64 352) (- 600 128) 352 112]]
          (draw/sprite! sprite/pixel x y w h {:color color/black})
          (draw/box! x y w h)
          (loop [creatures (:creatures party)
                 i 0]
            (when (< i (count creatures))
              (let [creature (nth creatures i)]
                (draw/centered-string! (:name creature) (+ x 3) (+ y 3 (* i 16)) w 24)
                (recur creatures (inc i)))))))
      ;;overlayed text
      (draw-log! g)
      (draw/string! (str "fps: " (gdx/get-fps)) (- 800 128) 0 512 32)
      (draw/string! (str "active: " (:active g)) (- 800 128) 16 512 32)
      (draw/string! (str "tick: " (:tick g)) (- 800 128) 32 512 32)
      (draw/string! (str "loglen: " (count (:log g))) (- 800 128) 48 512 32))
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