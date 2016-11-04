(ns falx.core
  (:require [falx.gdx :as gdx]
            [falx.pos :as pos]
            [falx.db :as db]
            [falx.engine :as engine]))

(def db
  (db/db (merge pos/schema)))

(def game
  (engine/game
    (engine/->GameState {} {} {})))

(def current-frame
  (atom nil))

(defn render!
  [frame]
  (gdx/draw! (str (:fps (gdx/frame-stats))) 0 0 64 64)
  (gdx/draw! (str (:elapsed-time frame)) 0 24 256 64))

(def renderer
  (memoize
    (partial engine/renderer #'render!)))

(defn frame!
  "Called every frame"
  []
  (try
    (let [gs @game
          frame (swap! current-frame
                       engine/next-frame
                       gs
                       (engine/current-input)
                       (engine/current-time))
          w 800
          h 600]
      ((renderer w h) frame))
    (catch Throwable e
      (println e)
      (Thread/sleep 5000))))

;; Gdx setup

(defn start!
  []
  (gdx/lwjgl-app
    #'frame!
    :size [800 600]
    :title "Falx"))

(comment
  (start!))