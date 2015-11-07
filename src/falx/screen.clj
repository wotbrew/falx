(ns falx.screen
  (:require [falx.entity :as entity]
            [falx.rect :as rect]
            [falx.graphics :as graphics]))

(defn mouse-in?
  [mouse rect]
  (rect/contains-point? rect (:point mouse)))

(defn update-text-button
  [m game rect]
  (cond
    (:disabled? m) m
    :else
    (assoc m :highlighted? (mouse-in?  (:mouse game) rect))))
