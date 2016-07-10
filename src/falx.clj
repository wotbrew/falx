(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [clojure.core.async :as async]
            [falx.geom :as g]
            [falx.draw :as draw]
            [falx.sprite :as sprite]))

(def max-fps
  60)

(def app
  (merge
    gdx/default-app
    {:max-background-fps max-fps
     :max-foreground-fps max-fps}))

(def game
  (atom {}))

(def in
  (async/chan))

(defn render!
  [game]
  (draw/draw!
    [(draw/vspacing 16 [(gdx/get-fps) @gdx/mouse-state])
     (draw/at (draw/img sprite/mouse-point)
              (g/point-tuple->geom (:point @gdx/mouse-state)))]))

(gdx/defrender
  (let [g @game]
    (try
      (render! g)
      (catch Throwable e
        (error e)
        (Thread/sleep 5000)))))

(defn -main
  [& args]
  (gdx/start-app! app))