(ns falx.core
  (:require [falx.gdx :as gdx]
            [falx.game :as g]
            [falx.ui :as ui]
            [falx.options]
            [falx.roster :as roster]
            [falx.character]
            [falx.inventory]))

(ui/defscene :continue
  ui/back-handler
  ui/breadcrumbs
  (ui/center
    (ui/resize
      320 280
      (ui/stack
        (ui/fancy-box 2)
        (ui/center "continue")))))

(defmethod ui/scene-name :continue [_] "Continue")

(ui/defscene :play
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
              roster/character-details)))))))

(defmethod ui/scene-name :play [_] "Play")

(def main-menu
  (ui/stack
    ui/breadcrumbs
    (ui/center
      (ui/resize
        320 280
        (ui/rows
          (ui/button "Play"
            :on-click [ui/goto :play]
            :hover-over "Start here")
          (ui/button "Roster"
                     :on-click [ui/goto :roster]
                     :hover-over "Manage your characters, load games and more")
          (ui/button "Continue"
                     :on-click [ui/goto :continue]
                     :hover-over "Continue from where you last left off")
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
        [w h] (:size (:config tick))]
    (ui/handle!
      (ui/scene frame)
      frame)))

(defn init!
  []
  (g/set-state! playing {}))

(comment
  (init!))
