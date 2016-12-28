(ns falx.core
  (:require [falx.gdx :as gdx]
            [falx.state :as state]
            [falx.ui :as ui]
            [falx.options]))

(ui/defscene :roster
  ui/back-handler
  (ui/center
    (ui/resize
      320 280
      (ui/stack
        (ui/fancy-box 2)
        (ui/center "roster")))))

(ui/defscene :new
  ui/back-handler
  (ui/center
    (ui/resize
      320 280
      (ui/stack
        (ui/fancy-box 2)
        (ui/center "new")))))

(ui/defscene :continue
  ui/back-handler
  (ui/center
    (ui/resize
      320 280
      (ui/stack
        (ui/fancy-box 2)
        (ui/center "continue")))))


(def main-menu
  (ui/center
    (ui/resize
      320 280
      (ui/rows
        (ui/button "Roster" :on-click [ui/goto :roster])
        (ui/button "New Adventure" :on-click [ui/goto :new])
        (ui/button "Continue Adventure" :on-click [ui/goto :continue])
        (ui/button "Options" :on-click [ui/goto :options])
        (ui/button "Quit" :on-click! (fn [_] (println "clicked quit...")))))))

(ui/defscene :main-menu
  main-menu)

(gdx/on-tick tick
  [tick]
  (let [frame (state/current-frame tick)
        [w h] (:size (:config tick))]
    (ui/handle!
      (ui/scene frame)
      frame)))

(defn init!
  [])