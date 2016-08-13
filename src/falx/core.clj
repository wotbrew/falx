(ns falx.core
  (:require [falx.gdx :as gdx]
            [falx.engine.ui :as ui]
            [clojure.tools.logging :refer [error info debug]]
            [falx.menu :as menu]
            [falx.config :as config]
            [falx.game :as g]
            [falx.engine.input :as input]))

(def max-fps
  60)

(def font
  (delay
    (gdx/bitmap-font)))

(def screens*
  {:falx.screen/menu #'menu/scene})

(def screens
  (if config/optimise?
    (into {} (map (juxt key (comp ui/scene var-get val))) screens*)
    screens*))

(def input
  (atom nil))

(gdx/defrender
  (try
    (let [gs @g/state
          scene (screens (:falx/screen gs :falx.screen/menu))
          scene-rect  [0 0 800 600]
          input (swap! input input/combine (input/now))
          events (ui/handle scene gs input scene-rect)]
      (doseq [event events]
        (prn events))
      (ui/draw! scene gs input scene-rect))
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(defn -main
  [& args]


  (gdx/start-lwjgl!
    {:max-foreground-fps 60
     :max-background-fps 60
     :size [800 600]}))