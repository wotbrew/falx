(ns falx.core
  (:require [falx.gdx :as gdx]
            [falx.state :as state]
            [falx.ui :as ui]
            [falx.options]
            [falx.roster]
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

(def main-menu
  (ui/stack
    ui/breadcrumbs
    (ui/center
      (ui/resize
        320 280
        (ui/rows
          (ui/button "Roster"
                     :on-click [ui/goto :roster]
                     :hover-over "Start here")
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

(gdx/on-tick tick
  [tick]
  (let [frame (state/current-frame tick)
        [w h] (:size (:config tick))]
    (ui/handle!
      (ui/scene frame)
      frame)))

(defn init!
  [])