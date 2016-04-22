(ns falx.impl.camera
  (:require [falx.action :as action]
            [falx.point :as point]))

(def move-factor
  0.4)

(defmethod action/action :move-camera
  [g {:keys [direction speed]
      :or {speed 1.0
           direction [0 0]}}]
  (let [delta (:delta-ms (:time g) 0)]
    (update-in g [:ui :viewport :camera :point]
               (fnil point/add [0 0])
               (point/scale direction
                            (* move-factor speed delta)))))