(ns falx.frame
  (:require [clj-gdx :as gdx]
            [falx.input :as input]))

(defn get-current-frame
  [world]
  {:delta (gdx/get-delta-time)
   :fps (gdx/get-fps)
   :display (gdx/get-display)
   :input (input/get-current-input)
   :world world})

