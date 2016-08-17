(ns falx.core
  (:require [falx.gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [falx.screen :as screen]
            [falx.engine.input :as input]
            [falx.engine.keyboard :as keyboard]
            [falx.menu]
            [falx.options]))

(def max-fps
  60)

(def font
  (delay
    (gdx/bitmap-font)))

(def input-state
  (atom nil))

(def screen-state
  (atom (screen/screen ::screen/id.menu [800 600])))

(gdx/defrender
  (try
    (let [input (swap! input-state input/combine (input/now))
          screen (swap! screen-state screen/handle input)
          screen (if (input/check input (input/hit ::keyboard/key.esc))
                   (swap! screen-state screen/back)
                   screen)]
      (screen/draw! screen input))
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(defn -main
  [& args]


  (gdx/start-lwjgl!
    {:max-foreground-fps 60
     :max-background-fps 60
     :size [800 600]}))