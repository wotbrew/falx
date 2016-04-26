(ns falx.impl.viewport
  (:require [falx.ui :as ui]
            [falx.action :as action]
            [falx.ui.viewport :as ui-viewport]
            [falx.point :as point]))

(defmethod ui/handle-click :viewport
  [g element point]
  (let [st (ui/get-state g element)
        point (ui-viewport/translate st point)
        level (:level st)
        cell {:level level
              :point point}]
    (-> g
        (action/action (action/select cell {:exclusive? true}))
        (action/action (action/move :selected cell)))))


(defmethod ui/handle-mod-click :viewport
  [g element point]
  (let [st (ui/get-state g element)
        point (ui-viewport/translate st point)
        level (:level st)
        cell {:level level
              :point point}]
    (-> g
        (action/action (action/select cell {:toggle? true})))))

(def move-factor
  0.4)

(defmethod action/action :move-camera
  [g {:keys [direction speed]
      :or {speed 1.0
           direction [0 0]}}]
  (let [delta (:visual-delta-ms (:time g) 0)]
    (update-in g [:ui :viewport]
               ui-viewport/move-camera
               (point/scale direction (* move-factor speed delta)))))