(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [clojure.core.async :as async]
            [falx.geom :as g]
            [falx.draw :as draw]
            [falx.sprite :as sprite]
            [falx.mouse :as mouse]
            [falx.game :as game]
            [falx.menu]))

(def max-fps
  60)

(def app
  (merge
    gdx/default-app
    {:max-background-fps max-fps
     :max-foreground-fps max-fps}))

(def game
  (atom game/defaults))

(defn render!
  [game]
  (draw/draw!
    [(draw/vspacing 16 [(::game/fps game)
                        (::mouse/point (::game/mouse game))
                        (:falx.button/mouse.left (::mouse/buttons (::game/mouse game)))
                        (:falx.button/mouse.right (::mouse/buttons (::game/mouse game)))])
     (draw/at (draw/img sprite/mouse-point)
              (::mouse/point (::game/mouse game)))
     (falx.menu/drawable (falx.menu/view game))]))

(gdx/defrender
  (try
    (let [g (swap! game game/next)]
      (render! g))
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(defn -main
  [& args]
  (gdx/start-app! app)
  (reset! game game/defaults))