(ns falx.core
  (:require [falx.gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [falx.screen.menu]
            [falx.screen.options]
            [falx.screen.main]
            [falx.frame :as frame]
            [falx.user :as user]))

(def max-fps
  60)

(def font
  (delay
    (gdx/bitmap-font)))

(def state
  (ref {}))

(def user
  (ref {}))

(gdx/defrender
  (try
    (frame/render @state @user)
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(def resolution-setting
  (user/setting ::user/setting.resolution))

(defn -main
  [& args]
  (gdx/start-lwjgl!
    {:max-foreground-fps 60
     :max-background-fps 60
     :vsync? false
     :size (resolution-setting)}))