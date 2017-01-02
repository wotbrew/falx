(ns falx.core
  (:require [falx.gdx :as gdx]
            [falx.game :as g]
            [falx.ui :as ui]
            [falx.options]
            [falx.roster :as roster]
            [falx.play :as play]
            [falx.inventory]
            [falx.game-state :as gs])
  (:import (com.badlogic.gdx Input$Keys)
           (com.badlogic.gdx.graphics Color)))

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
            :on-click [ui/goto :play]
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
        gs (:state frame)
        [w h :as size] (:size (:config tick))]
    (when (not= size (gs/setting gs :resolution))
      (g/update-state! playing assoc-in [:settings :resolution] size))
    (ui/handle!
      (ui/scene frame)
      frame)))

(defn add-goblins
  [gs pt]
  (let [ep  (g/gen-id playing)]
    (reduce
      (fn [gs id]
        (gs/add
          gs id
          {:id id
           :type :creature
           :party ep
           :race :goblin
           :gender :male
           :enemy? true}))
      (gs/add gs ep
              {:id ep
               :type :party
               :enemy? true
               :pt pt
               :level :test
               :cell {:level :test
                      :pt pt}
               :slice {:level :test
                       :layer :creature}
               :layer :creature})
      (repeatedly (inc (rand-int 6)) #(g/gen-id playing)))))

(defn put-floors
  [gs]
  (loop [gs gs
         pts (for [x (range 100)
                   y (range 100)]
               [x y])]
    (if-some [[pt & xs] (seq pts)]
      (let [[id gs] (gs/next-id gs)]
        (recur (gs/add gs id
                       {:id    id
                        :pt    pt
                        :cell  {:level :test
                                :pt    pt}
                        :slice {:level :test
                                :layer :floor}
                        :level :test
                        :layer :floor
                        :type  :floor})
               xs))
      gs)))

(g/set-state!
  playing
  (let [p  (g/gen-id playing)
        p1 (g/gen-id playing)
        p2 (g/gen-id playing)
        p3 (g/gen-id playing)
        p4 (g/gen-id playing)
        p5 (g/gen-id playing)
        p6 (g/gen-id playing)]
    (-> {:scene       :play
         :scene-stack [:play]
         :players [p1 p2 p3 p4 p5 p6]
         :settings {:cell-size [64 64]}}
        (put-floors)
        (gs/add p1
                (merge
                  (falx.character/genbody)
                  {:id      p1
                   :type :creature
                   :party p
                   :player? true}))
        (gs/add p2 (merge
                     (falx.character/genbody)
                     {:id      p2
                      :type :creature
                      :party p
                      :player? true}))
        (gs/add p3 (merge
                     (falx.character/genbody)
                     {:id      p3
                      :type :creature
                      :party p
                      :player? true}))
        (gs/add p4
                (merge
                  (falx.character/genbody)
                  {:id      p4
                   :type :creature
                   :party p
                   :player? true}))
        (gs/add p5 (merge
                     (falx.character/genbody)
                     {:id      p5
                      :type :creature
                      :party p
                      :player? true}))
        (gs/add p6 (merge
                     (falx.character/genbody)
                     {:id      p6
                      :type :creature
                      :party p
                      :player? true}))

        (gs/add p {:id p
                   :type :party
                   :player-party? true
                   :player? true
                   :lead p
                   :pt [3 4]
                   :level :test
                   :cell {:level :test
                          :pt [3 4]}
                   :slice {:level :test
                           :layer :creature}
                   :layer :creature})

        (add-goblins [4 4])
        (add-goblins [5 5])
        (add-goblins [2 3])
        (add-goblins [2 2])
        (add-goblins [2 4])
        (add-goblins [2 5])
        (add-goblins [3 5])
        (add-goblins [3 6]))))

(defn gs
  []
  (g/state playing))

(defn init!
  []
  (g/set-state! playing {})
  nil)

(comment
  (init!))
