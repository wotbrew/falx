(ns falx.core
  (:require [falx.gdx :as gdx]))

(def camera
  (delay @(gdx/run (gdx/camera 800 600))))

(def batch
  (delay @(gdx/run (gdx/batch))))

(def font
  (delay @(gdx/run (gdx/font))))

(defn frame!
  "Called every frame"
  []
  (try
    (gdx/render
      {:batch @batch
       :font @font
       :camera @camera}
      (gdx/draw! "foo" 32 32 64 32))
    (catch Throwable e
      (println e)
      (Thread/sleep 5000))))

;; Gdx setup

(defn -main
  []
  (gdx/lwjgl-app
    #'frame!
    :size [800 600]
    :title "Falx"))