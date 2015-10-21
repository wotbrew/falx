(ns falx.core
  (:gen-class)
  (:require [clojure.tools.logging :refer [info error]]
            [falx.application :as app]
            [falx.frame :as frame]
            [falx.graphics.text :as text]
            [falx.graphics.camera :as camera]
            [falx.graphics.image :as image]
            [falx.graphics.screen :as screen]
            [falx.ui.widgets :as widgets]
            [falx.ui.screens.main :as main]))

(defn run-frame!
  []
  (let [frame (frame/update!)]
    (screen/clear!)
    (camera/use-game-camera!)
    (camera/use-ui-camera!)
    (-> [(main/get-menu (screen/get-size))
         widgets/fps-counter]
        widgets/panel
        (widgets/draw! frame))))

(def screen-size [1024 768])

(defn -main
  [& args]
  (app/application #'run-frame!)
  (screen/set-size! screen-size)
  (camera/set-size! screen-size))
