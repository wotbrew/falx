(ns falx.core
  (:require [falx.gdx :as gdx]
            [falx.pos :as pos]
            [falx.db :as db]))

(def db
  (db/db (merge pos/schema)))

(defn frame!
  "Called every frame"
  []
  (try

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

(comment
  (-main))