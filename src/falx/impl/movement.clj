(ns falx.action.movement
  (:require [falx.action :as action]
            [falx.world :as world]))

(defn resolve-targets
  [g target]
  (cond
    (= :selected target) (:selected (:player g))
    (vector? target) (mapcat (partial resolve-targets g) target)
    :else [target]))

(defn resolve-dest
  [g destination]
  destination)

(defmethod action/action :move
  [g {:keys [target dest]}]
  (let [ids (resolve-targets g target)
        cell (resolve-dest g dest)]
    g
    #_(update g :ai (partial merge-with merge) (->> (for [id ids]
                                                    [id {:activity {:type :move
                                                                    :goal cell}}])
                                                  (into {})))))