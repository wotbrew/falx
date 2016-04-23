(ns falx.impl.input
  (:require [falx.action :as action]
            [clojure.set :as set]
            [falx.ui.mouse :as ui-mouse]))

(def default-bind
  {[:game :game] {:cam-up #{[:pressed :key :w]}
                  :cam-down #{[:pressed :key :s]}
                  :cam-left #{[:pressed :key :a]}
                  :cam-right #{[:pressed :key :d]}

                  :select #{[:hit :button :left]}}})

(def action-map
  {})

(def get-action nil)

(defmulti get-action (fn [g modified? mouse-state k] k))

(defmethod get-action :default
  [_ _ _ k]
  (get action-map k))

(defmethod get-action :cam-up
  [_ modified? _ _]
  {:type :move-camera
   :direction [0 -1]
   :speed (if modified? 2.0 1.0)})

(defmethod get-action :cam-left
  [_ modified? _ _]
  {:type :move-camera
   :direction [-1 0]
   :speed (if modified? 2.0 1.0)})

(defmethod get-action :cam-right
  [_ modified? _ _]
  {:type :move-camera
   :direction [1 0]
   :speed (if modified? 2.0 1.0)})

(defmethod get-action :cam-down
  [_ modified? _ _]
  {:type :move-camera
   :direction [0 1]
   :speed (if modified? 2.0 1.0)})

(defmethod get-action :select
  [g modified? mouse-state _]
  {:type :select
   :exclusive? (not modified?)
   :toggle? modified?
   :target (:cell mouse-state)})

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
        bindings (when (seq iset) (get-active-bindings g iset))
        mouse-state (ui-mouse/get-state (:display g) (:viewport (:ui g)) (:point mouse))
        modified? (-> keyboard :pressed (contains? :shift-left))]
    (cons
      {:type :move-mouse
       :point (:point mouse)}
      (keep #(get-action g modified? mouse-state %) bindings))))

(defmethod action/action :handle-input
  [g {:keys [keyboard mouse]}]
  (reduce action/action g (get-actions g keyboard mouse)))