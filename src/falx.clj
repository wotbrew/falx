(ns falx
  (:require [falx.gdx :as gdx]
            [falx.draw :as d]
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
    (d/draw!
      (d/each
        (d/box)
        (d/text "foo" {:centered? true})
        ["frame: " (gdx/frame-id) "delta: " (gdx/delta-time)])
      0 32 256 256)
    (d/each!
      [0 0 96 32]
      (d/box {:color [1 1 1 1]})
      (d/center (gdx/fps)))
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(defn -main
  [& args]
  (gdx/start-lwjgl!
    {:max-foreground-fps 60
     :max-background-fps 60
     :size [800 600]}))