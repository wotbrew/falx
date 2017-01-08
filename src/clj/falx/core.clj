(ns falx.core
  (:require [falx.gdx :as gdx]
            [falx.game :as g]
            [falx.ui :as ui]
            [falx.options]
            [falx.roster :as roster]
            [falx.play :as play]
            [falx.inventory]
            [falx.game-state :as gs])
  (:import (java.util UUID)))

(ui/defscene :load
  ui/back-handler
  ui/breadcrumbs
  (ui/center
    (ui/resize
      320 280
      (ui/stack
        (ui/fancy-box 2)
        (ui/center "load")))))

(defmethod ui/scene-name :load [_] "Load")

(ui/defscene :new
  ui/back-handler
  ui/breadcrumbs
  (ui/pad 32 50
    (ui/stack
      (ui/restrict-height 32
        (ui/cols
          (ui/button "Roster"
            :on-click [ui/goto :roster])
          roster/stats-button
          roster/inventory-button
          (ui/button "Back")))
      (ui/pad 0 48
        (ui/cols
          (ui/pad 3 0
            (ui/rows
              (ui/fancy-box 1)
              (ui/pad 0 3
                (ui/fancy-box 1))))
          (ui/rows roster/character-array
            (ui/pad 0 3
              (ui/fancy-box 1)
              roster/character-details))))))
  ui/hover-over)

(defmethod ui/scene-name :new [_] "New")

(def main-menu
  (ui/stack
    ui/back-handler
    ui/breadcrumbs
    (ui/center
      (ui/resize
        320 280
        (ui/rows
          (ui/button "TESTING"
            :on-click [ui/goto-no-follow :play]
            :hover-over "Start here")
          (ui/button "New"
            :on-click [ui/goto :new]
            :hover-over "Start here")
          (ui/button "Load"
            :on-click [ui/goto :load]
            :hover-over "Start here")
          (ui/button "Roster"
            :on-click [ui/goto :roster]
            :hover-over "Manage your existing characters")
          (ui/button "Options"
            :on-click [ui/goto :options]
            :hover-over "Change options and game settings")
          (ui/button "Quit"
            :on-click! (fn [_] (println "clicked quit..."))))))
    ui/hover-over))

(ui/defscene :main-menu
  main-menu)

(defmethod ui/scene-name :main-menu [_] "Menu")

(ui/defscene :default
  main-menu)

(defmethod ui/scene-name :default [_] "Menu")

(defonce playing
  (g/game))

(gdx/on-tick tick
  [tick]
  (let [frame (g/next-frame playing tick)
        g (g/game (:game frame))
        gs (:state frame)
        size (:size (:config tick))]
    (when (not= size (gs/resolution gs))
      (g/update-state! g gs/set-resolution size))
    (ui/handle!
      (ui/scene frame)
      frame)
    (g/update-state! g gs/simulate (:delta tick))))

(defn goblins-tx-data
  [pt]
  (let [ep (UUID/randomUUID)]
    (cons
      {:id  ep
       :type   :party
       :enemy? true
       :solid? true
       :pt     pt
       :level  :test
       :cell   {:level :test
                :pt    pt}
       :slice  {:level :test
                :layer :creature}
       :layer  :creature}
      (for [_ (range (inc (rand-int 6)))]
        {:type   :creature
         :party  ep
         :race   :goblin
         :gender :male
         :enemy? true}))))

(defn floor-tx-data
  []
  (for [x (range 100)
        y (range 100)
        :let [pt [x y]]]
    {:pt    pt
     :cell  {:level :test
             :pt    pt}
     :slice {:level :test
             :layer :floor}
     :level :test
     :layer :floor
     :type  :floor}))

(g/set-state!
  playing
  (let [p  (gs/tempid)
        players (vec (repeatedly 6 gs/tempid))]
    (-> gs/empty
        (gs/transact
          (concat
            (floor-tx-data)
            (goblins-tx-data [4 4])
            (goblins-tx-data [5 5])
            (goblins-tx-data [2 3])
            (goblins-tx-data [2 2])
            (goblins-tx-data [2 4])
            (goblins-tx-data [2 5])
            (goblins-tx-data [3 5])
            (goblins-tx-data [3 6])
            [{:id            p
              :type          :party
              :player-party? true
              :solid?        true
              :pt            [3 4]
              :level         :test
              :cell          {:level :test
                              :pt    [3 4]}
              :slice         {:level :test
                              :layer :creature}
              :layer         :creature}]
            (for [pl players]
              (merge
                (falx.character/genbody)
                {:id pl
                 :type :creature
                 :party p
                 :solid? true
                 :player? true}))))
        (merge
          {:scene        :play
           :scene-stack  [:main-menu :play]
           :active-party p
           :players      players})
        (gs/center-camera-on-pt [3 4]))))

(defn init!
  []
  (g/set-state! playing gs/empty)
  nil)

(comment
  (init!))
