(ns falx.menu
  (:require [falx.ui :as ui]
            [falx.scene :as scene]
            [falx.debug :as debug]))

(def scene
  (scene/stack
    (scene/fit #'debug/table 400 72)
    (scene/center
      (scene/rows
        (ui/button "New" {:click (fn [gs _] (assoc gs :falx/screen :falx.screen/main))})
        (ui/button "Continue"{:click (fn [gs _] (println "continue") gs)})
        (ui/button "Options"{:click (fn [gs _] (println "options") gs)})
        (ui/button "Exit"{:click (fn [gs _] (println "exit") gs)}))
      [320 320])))