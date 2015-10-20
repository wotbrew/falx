(ns falx.core
  (:gen-class)
  (:require [clojure.tools.logging :refer [info error]]
            [falx.application :as app]
            [falx.graphics.text :as text]
            [falx.graphics.camera :as camera]
            [falx.graphics.image :as image]
            [falx.graphics.screen :as screen]))

(defn run-frame!
  []
  (screen/clear!)
  (camera/use-game-camera!)
  (image/draw! :staff 0 0)
  (image/draw! :staff 32 32)
  (image/draw! :staff 64 64)
  (camera/use-ui-camera!)
  (text/draw! (app/get-fps) 0 0))

(defn -main
  [& args]
  (app/application #'run-frame!))
