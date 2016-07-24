(ns falx.frame
  (:require [clj-gdx :as gdx]))

(defn current
  []
  {::fps (gdx/get-fps)
   ::delta (gdx/get-delta-time)
   ::frame-id (gdx/get-frame-id)})

