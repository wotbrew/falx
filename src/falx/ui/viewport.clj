(ns falx.ui.viewport
  (:require [falx.point :as point]))

(defn- translate-point
  [[x y] [cx cy] [cw ch]]
  [(int (/ (+ cx x) cw))
   (int (/ (+ cy y) ch))])

(defn translate
  [st point]
  (let [camera (:camera st)]
    (translate-point point (:point camera) (:cell-size st))))

(defn move-camera
  [st point]
  (update-in st [:camera :point] (fnil point/add [0 0]) point))

(defn viewport-element
  [rect]
  {:id :viewport
   :type :viewport
   :rect rect
   :children []})