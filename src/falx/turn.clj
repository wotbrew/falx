(ns falx.turn
  (:require [falx.game :as g]
            [falx.action :as action]))

(defn begin-player
  [g]
  (-> (g/say g "Now you may move...")
      (g/select-creature 0)))

(defn end
  ([g]
   (if-some [active (:active g)]
     (-> (g/say g "AI Moving...")
         (dissoc :active)
         (g/schedule-in [:begin-turn active] 10))
     g))
  ([g id]
    (if (g/active? g id)
      (end g)
      g)))

(action/bind :end-turn #'end)

(defn begin
  [g id]
  (let [g (assoc g :active id)]
    (if (g/player? id)
      (begin-player g)
      (action/run g
                  #_[:end-turn id]
                  [:move-towards id :player]))))

(action/bind :begin-turn #'begin)