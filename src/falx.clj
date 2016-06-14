(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]))

(def max-fps
  60)

(def app
  (merge
    gdx/default-app
    {:max-background-fps max-fps
     :max-foreground-fps max-fps}))

(gdx/defrender
  (gdx/draw-string! (gdx/get-fps) 0 0 128))

(defn -main
  [& args]
  (gdx/start-app! app))