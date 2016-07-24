(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [falx.frame :as frame]
            [falx.ui.menu :as menu]
            [falx.ui :as ui]
            [falx.scene :as scene]
            [falx.keyboard :as keyboard]
            [falx.mouse :as mouse]))

(def max-fps
  60)

(def app
  (merge
    gdx/default-app
    {:max-background-fps max-fps
     :max-foreground-fps max-fps}))

(defonce gs (atom {}))

(gdx/defrender
  (try
    (let [old-gs @gs
          frame (frame/current)
          keyboard (keyboard/current old-gs)
          mouse (mouse/current old-gs)
          gs (swap! gs merge frame keyboard mouse)
          layout (scene/layout menu/scene [0 0 800 600])
          gs (ui/handle gs layout)]
      (ui/draw! layout gs))
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))
(defn -main
  [& args]
  (gdx/start-app! app))