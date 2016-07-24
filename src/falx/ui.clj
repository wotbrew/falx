(ns falx.ui
  (:require [falx.draw :as d]
            [falx.rect :as rect]))

(defn state
  [gs k]
  (-> gs ::state (get k)))

(defn reset-state
  [gs k state]
  (assoc-in gs [::state k] state))

(defn alter-state
  ([gs k f]
   (let [st (state gs k)]
     (reset-state gs k (f st))))
  ([gs k f & args]
   (alter-state gs k #(apply f % args))))

(defonce ^:private registry
  (atom {}))

(defrecord Elem [view
                 drawfn
                 handler])

(def noop
  (constantly nil))

(def defaults
  {:view identity
   :draw (constantly noop)
   :handler (fn [gs rect] gs)})

(defn elem
  [m]
  (map->Elem (merge defaults m)))

(defn add-elem!
  [k m]
  (swap! registry assoc k (elem m))
  nil)

(defn defelem
  [k & {:as kvs}]
  (add-elem! k kvs))

(defn draw!
  [layout gs]
  (let [elem @registry]
    (->
      (fn [[k rect]]
        (when-some [elem (elem k)]
          (let [vfn (:view elem)
                view (vfn gs)
                ddraw! ((:draw elem) view rect)]
            (when ddraw! (ddraw!)))))
      (run! layout))))

(defn handle
  [gs layout]
  (let [elem @registry]
    (->
      (fn [gs [k rect]]
        (if-some [elem (elem k)]
          ((:handler elem) gs rect)
          gs))
      (reduce gs layout))))