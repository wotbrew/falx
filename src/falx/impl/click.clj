(ns falx.impl.click
  (:require [falx.action :as action]
            [falx.screen :as screen]
            [falx.ui :as ui]))

(defmethod action/action :click
  [g click]
  (let [[width height] (-> g :display :screen-size (or [1024 768]))]
    (if (:modified? click)
      (ui/mod-click g (screen/screen (:screen g) width height) (:point click))
      (ui/click g (screen/screen (:screen g) width height) (:point click)))))

(defmethod action/action :alt-click
  [g click]
  (let [[width height] (-> g :display :screen-size (or [1024 768]))]
    (ui/alt-click g (screen/screen (:screen g) width height) (:point click))))