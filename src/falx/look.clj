(ns falx.look
  (:require [falx.game :as g]
            [falx.action :as action]
            [falx.cursor :as cursor]
            [falx.thing :as thing]
            [falx.point :as point])
  (:import (clojure.lang PersistentQueue)))

(defn looking?
  [g]
  (and (g/active? g :cursor)
       (= (:type (:cursor g)) :look)))

(defn begin-look
  [g]
  (if (g/player-active? g)
    (-> (assoc g :active :cursor
                 :cursor {:type :look
                          :queue (into PersistentQueue/EMPTY (map :id)
                                       (when-some [point (g/get-point g :player)]
                                         (sort-by #(point/get-manhattan-distance (:point % [0 0]) point)
                                                  (g/get-at-layer g :creature))))
                          :rqueue (list)
                          :point (or (g/get-point g :player) [0 0])})
        (cursor/move-next))
    g))

(action/bind :begin-look #'begin-look)

(defmethod cursor/on-move :look
  [g {:keys [point]}]
  (-> (g/say g (str "looking at: " point))
      (assoc :focus (first (map :id (filter thing/party? (g/get-at g point)))))))