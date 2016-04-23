(ns falx.ui.viewport
  (:require [falx.point :as point]))

(defn move-camera
  [st point]
  (update-in st [:camera :point] (fnil point/add [0 0]) point))