(ns falx.loop
  (:require [clj-gdx :as gdx]))

(defn current-frame
  []
  {::fps (gdx/get-fps)
   ::delta (gdx/get-delta-time)
   ::frame-id (gdx/get-frame-id)})

(defn handle-frame
  [gs frame]
  (assoc gs ::frame frame))