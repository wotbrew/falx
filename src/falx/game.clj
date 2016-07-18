(ns falx.game
  (:require [falx.mouse :as mouse]
            [clj-gdx :as gdx]
            [falx.geom :as g])
  (:refer-clojure :exclude [next]))

(defn get-frame-map
  []
  {::fps (gdx/get-fps)
   ::frame-id (gdx/get-frame-id)
   ::delta (gdx/get-delta-time)})

(defn next
  [g]
  (let [gdx-mouse @gdx/mouse-state
        frame (get-frame-map)]
    (merge g
           frame
           {::mouse (mouse/next (::mouse g) gdx-mouse (::delta frame))})))

(def defaults
  {::cell-size (g/size 32 32)
   ::screen-size (g/size 800 600)
   ::mouse mouse/initial
   ::fps 60
   ::frame-id 0
   ::delta 0.0})