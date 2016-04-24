(ns falx.impl.viewport
  (:require [falx.ui :as ui]
            [falx.action :as action]
            [falx.ui.viewport :as ui-viewport]))

(defmethod ui/handle-click :viewport
  [g element point]
  (let [st (ui/get-state g element)
        point (ui-viewport/translate st point)
        level (:level st)
        cell {:level level
              :point point}]
    (-> g
        (action/action {:type :select
                        :exclusive? true
                        :target cell})
        (action/action {:type :move
                        :target :selected
                        :dest cell}))))


(defmethod ui/handle-mod-click :viewport
  [g element point]
  (let [st (ui/get-state g element)
        point (ui-viewport/translate st point)
        level (:level st)
        cell {:level level
              :point point}]
    (-> g
        (action/action {:type :select
                        :toggle? true
                        :target cell}))))