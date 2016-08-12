(ns falx.core
  (:require [falx.gdx :as gdx]
            [falx.ui :as ui]
            [clojure.tools.logging :refer [error info debug]]
            [falx.menu :as menu]
            [falx.main :as main]
            [falx.state :as state]
            [falx.config :as config]
            [falx.db :as db]
            [falx.creature :as creature]
            [falx.party :as party]
            [falx.world :as world]
            [falx.entity :as entity]
            [falx.game :as g]
            [clojure.core.async :as async]))

(def max-fps
  60)

(def font
  (delay
    (gdx/bitmap-font)))

(def screens*
  {:falx.screen/menu #'menu/scene
   :falx.screen/main #'main/scene})

(def screens
  (if config/optimise?
    (into {} (map (juxt key (comp ui/scene var-get val))) screens*)
    screens*))

(gdx/defrender
  (try
    (let [frame @state/frame
          gs (async/<!! (g/gswap! state/splice frame))
          scene (screens (:falx/screen gs :falx.screen/menu))
          scene-rect  [0 0 800 600]
          gs (async/<!! (g/gswap! (partial ui/handle scene) scene-rect))]
      (ui/draw! scene gs scene-rect))
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(defn -main
  [& args]


  (g/gswap!
    (constantly
      (-> {}

          (world/add
            (entity/put
              (creature/creature
                ::creature/player? true)
              (entity/pos :falx.level/limbo [3 4])))

          (world/add
            (entity/put
              (creature/creature
                ::creature/player? true)
              (entity/pos :falx.level/limbo [8 8]))))))

  (gdx/start-lwjgl!
    {:max-foreground-fps 60
     :max-background-fps 60
     :size [800 600]}))