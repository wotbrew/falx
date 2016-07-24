(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [falx.draw :as d]
            [gdx.color :as color]))

(def max-fps
  60)

(def app
  (merge
    gdx/default-app
    {:max-background-fps max-fps
     :max-foreground-fps max-fps}))

(gdx/defrender
  (try
    (d/draw! (d/button "foobar") 32 32 96 30)
    (d/draw! (d/button "foobar" :ui.button/state.focused) 32 64 96 30)
    (d/draw! (d/button "foobar" :ui.button/state.disabled) 32 96 96 30)

    (d/draw! d/torch 128 128 32 32)
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(defn -main
  [& args]
  (gdx/start-app! app))