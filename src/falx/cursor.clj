(ns falx.cursor
  (:require [falx.action :as action]
            [falx.game :as g]
            [falx.point :as point]))

(defn get-direction-vector
  [direction]
  (if (vector? direction)
    direction
    (case direction
      :nw [-1 -1]
      :n [0 -1]
      :ne [1 -1]
      :w [-1 0]
      :sw [-1 1]
      :s [0 1]
      :se [1 1]
      :e [1 0])))

(defmulti on-move (fn [g cursor] (:type cursor)))

(defmethod on-move :default
  [g cursor]
  g)

(defn move
  [g direction]
  (if-not (g/active? g :cursor)
    g
    (let [v (get-direction-vector direction)]
      (as-> g g
            (update-in g [:cursor :point] point/add v)
            (on-move g (:cursor g))))))

(action/bind :move-cursor #'move)

(defn cancel
  [g]
  (if (g/active? g :cursor)
    (-> (assoc g :active :player)
        (dissoc :cursor :focus))
    g))

(action/bind :cancel-cursor #'cancel)

(defn move-back
  [g]
  (if (and (g/active? g :cursor)
           (seq (-> g :cursor :rqueue)))
    (let [{:keys [rqueue]} (:cursor g)
          next (peek rqueue)
          rqueue (pop rqueue)
          point (g/get-point g next)]
      (if point
        (as-> g g
              (update g :cursor merge {:rqueue rqueue
                                       :point point})
              (on-move g (:cursor g)))
        g))
    g))

(action/bind :move-back-cursor #'move-back)

(defn move-next
  [g]
  (if (g/active? g :cursor)
    (let [{:keys [queue rqueue last]} (:cursor g)
          next (peek queue)
          queue (-> (pop queue) (conj next))
          rqueue (if last (conj rqueue last) rqueue)
          point (g/get-point g next)]
      (if point
        (as-> g g
              (update g :cursor merge
                    {:queue queue
                     :rqueue rqueue
                     :last next
                     :point point})
            (on-move g (:cursor g)))
        g))
    g))

(action/bind :move-next-cursor #'move-next)