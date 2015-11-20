(ns falx.screen.play
  (:require [falx.widget :as widget]))

(defn screen
  [game size]
  (let [[w h] size]
    {:type :play/screen
     :game game
     :rect [0 0 w h]}))

(defmethod widget/on-frame :play/screen
  [m game]
  (assoc m :game game))

(defmethod widget/get-screen :play
  [_ m game]
  (screen game (-> game :ui-camera :size)))

