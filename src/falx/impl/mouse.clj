(ns falx.impl.mouse
  (:require [falx.action :as action]
            [falx.ui.mouse :as ui-mouse]))

(defmethod action/action :move-mouse
  [g {:keys [point]}]
  (let [{:keys [ui display]} g
        viewport (:viewport ui)]
    (assoc-in g [:ui :mouse] (ui-mouse/get-state display viewport point))))