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

(def ^:private registry
  (atom {}))

(defrecord Elem [view drawfn])

(def noop
  (constantly nil))

(defn elem
  [m]
  (map->Elem (merge
               {:view identity
                :drawfn (constantly noop)}
               m)))

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
                ddraw! ((:drawfn elem) view rect)]
            (when ddraw! (ddraw!)))))
      (run! layout))))