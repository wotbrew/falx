(ns falx.core
  (:gen-class)
  (:require [clojure.tools.logging :refer [info error]]
            [falx.application :as app]
            [falx.graphics.camera :as camera]
            [falx.graphics.image :as img]
            [falx.graphics.screen :as screen]))

(defn run-frame!
  []
  (screen/clear!)
  (camera/use-game-camera!)
  (img/draw! :staff 0 0)
  (img/draw! :staff 32 32)
  (img/draw! :staff 64 64)
  (camera/use-ui-camera!))

(defn -main
  [& args]
  (app/application #'run-frame!))
