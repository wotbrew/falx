(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [falx.menu]))

(def max-fps
  60)

(def app
  (merge
    gdx/default-app
    {:max-background-fps max-fps
     :max-foreground-fps max-fps}))

(gdx/defrender
  (try
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(defn -main
  [& args]
  (gdx/start-app! app))