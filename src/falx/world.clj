(ns falx.world
  (:require [falx.game :as g]
            [falx.position :as pos]
            [falx.actor :as a]
            [falx.event :as event]))

(defn set-pos
  [g id cell]
  (let [a (g/get-actor g id)
        old-cell (:cell a)]
    (if (= old-cell cell)
      g
      (as-> g x
            (g/update-actor x id a/set-pos cell)
            (g/publish x (event/pos-changed a (g/get-actor x id)))))))

(defn rem-pos
  [g id]
  (let [a (g/get-actor g id)]
    (if (identical? ::not-found (:cell a ::not-found))
      g
      (as-> g x
            (g/update-actor x id a/rem-pos)
            (g/publish x (event/pos-removed a (g/get-actor x id)))))))