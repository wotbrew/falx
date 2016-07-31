(ns falx
  (:require [falx.gdx :as gdx]
            [falx.draw :as d]
            [falx.ui :as ui]
            [clojure.tools.logging :refer [error info debug]]
            [falx.gdx.display :as display]
            [falx.scene :as scene]))

(def max-fps
  60)

(def state
  (atom {}))

(def font
  (delay
    (gdx/bitmap-font)))

(def scene
  (scene/rows
    (ui/env (d/center (str "fps: " (gdx/fps))))
    (scene/cols (d/box)
                (d/box)
                (d/box))
    (scene/rows
      (scene/cols
        (d/center "foo")
        (d/center "hello"))
      (d/in-box (d/center "bar")))
    (d/center "foo")))

(def ui
  (ui/scene
    scene
    {:cache-layout? false
     :cache-draw? false
     :cache-handle? false}))

(gdx/defrender
  (try
    (ui/draw! ui {} [0 0 800 600])
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(defn -main
  [& args]
  (gdx/start-lwjgl!
    {:max-foreground-fps 60
     :max-background-fps 60
     :size [800 600]}))