(ns falx
  (:require [falx.gdx :as gdx]
            [falx.ui :as ui]
            [clojure.tools.logging :refer [error info debug]]
            [falx.menu :as menu]
            [falx.main :as main]
            [falx.state :as state]))

(def max-fps
  60)

(def state
  (atom {}))

(def font
  (delay
    (gdx/bitmap-font)))

(def screens
  {:falx.screen/menu #'menu/scene
   :falx.screen/main #'main/scene})

(gdx/defrender
  (try
    (let [frame @state/frame
          gs (swap! state state/splice frame)
          scene (screens (:falx/screen gs :falx.screen/menu))
          scene-rect  [0 0 800 600]
          gs (swap! state (partial ui/handle scene) scene-rect)]
      (ui/draw! scene gs scene-rect))
    (catch Throwable e
      (error e)
      (Thread/sleep 10000))))

(defn -main
  [& args]
  (gdx/start-lwjgl!
    {:max-foreground-fps 60
     :max-background-fps 60
     :size [800 600]}))