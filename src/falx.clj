(ns falx
  (:require [falx.gdx :as gdx]
            [falx.draw :as draw]
            [clojure.tools.logging :refer [error info debug]]
            [falx.gdx.display :as display]))

(def max-fps
  60)

(def state
  (atom {}))

(def font
  (delay
    (gdx/bitmap-font)))

(gdx/defrender
  (try
    (draw/draw! (draw/box {:color [1 1 1 1]}) 0 0 96 32)
    (draw/draw! (gdx/fps) 6 6 96 32)
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(defn -main
  [& args]
  (gdx/start-lwjgl!
    {:max-foreground-fps 60
     :max-background-fps 60
     :size [800 600]}))