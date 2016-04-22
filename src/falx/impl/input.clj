(ns falx.impl.input
  (:require [falx.action :as action]
            [clojure.set :as set]))

(def default-bind
  {[:game :game] {:cam-up #{[:pressed :key :w]}
                  :cam-down #{[:pressed :key :s]}
                  :cam-left #{[:pressed :key :a]}
                  :cam-right #{[:pressed :key :d]}
                  :cam-up-fast #{[:pressed :key :w]
                                 [:pressed :key :shift-left]}
                  :cam-down-fast #{[:pressed :key :s]
                                   [:pressed :key :shift-left]}
                  :cam-left-fast #{[:pressed :key :a]
                                   [:pressed :key :shift-left]}
                  :cam-right-fast #{[:pressed :key :d]
                                    [:pressed :key :shift-left]}}})

(def action-map
  {
   ;; cameras
   :cam-up {:type :move-camera
            :direction [0 -1]}
   :cam-left {:type :move-camera
              :direction [-1 0]}
   :cam-right {:type :move-camera
               :direction [1 0]}
   :cam-down {:type :move-camera
              :direction [0 1]}

   :cam-up-fast {:type :move-camera
                 :direction [0 -1]
                 :speed 2.0}
   :cam-left-fast {:type :move-camera
                   :direction [-1 0]
                   :speed 2.0}
   :cam-right-fast {:type :move-camera
                    :direction [1 0]
                    :speed 2.0}
   :cam-down-fast {:type :move-camera
                   :direction [0 1]
                   :speed 2.0}})

(defn get-input-set
  "Gets the set of pressed/hit/keys/buttons"
  [keyboard mouse]
  (into #{} cat [(map (partial vector :hit :key) (:hit keyboard))
                 (map (partial vector :pressed :key) (:pressed keyboard))

                 (map (partial vector :hit :button) (:hit mouse))
                 (map (partial vector :pressed :button) (:pressed mouse))]))

(defn get-active-bindings
  [g iset]
  (for [[k v] (get (or (:bind g default-bind)) [(:screen g) (:context g)])
        :when (set/subset? v iset)]
    k))


(defn get-actions
  [g keyboard mouse]
  (let [iset (get-input-set keyboard mouse)
        bindings (when (seq iset) (get-active-bindings g iset))]
    (keep action-map bindings)))

(defmethod action/action :handle-input
  [g {:keys [keyboard mouse]}]
  (reduce action/action g (get-actions g keyboard mouse)))