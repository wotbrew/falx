(ns falx.game-state
  (:require [falx.util :as util]))

(defn next-id
  [gs]
  (let [id (inc (:id-seed gs 0))]
    [id (assoc gs :id-seed id)]))

(defn entity
  [gs id]
  (-> gs :eav (get id)))

(defn attr
  [gs id k]
  (get (entity gs id) k))

(defn del-attr
  ([gs id k]
    (-> gs
        (util/dissoc-in [:eav id k])
        (util/disjoc-in [:ave k (attr gs id k)] id))))

(defn add-attr
  ([gs id k v]
   (let [gs (del-attr gs id k)]
     (-> gs
         (assoc-in [:eav id k] v)
         (update-in [:ave k v] (fnil conj #{}) id)))))

(defn modify-attr
  ([gs id k f]
    (let [v (attr gs id k)]
      (add-attr gs id k (f v))))
  ([gs id k f & args]
    (modify-attr gs id k #(apply f % args))))

(defn add-attrs
  ([gs id m]
    (reduce-kv #(add-attr %1 id %2 %3) gs m)))

(defn del
  ([gs id]
    (reduce-kv
      (fn [gs k _] (del-attr gs id k))
      gs
      (entity gs id))))

(defn add
  ([gs id m]
    (-> (del gs id)
        (add-attrs id m))))

(defn modify
  ([gs id f]
    (let [e (entity gs id)]
      (add gs id (f e))))
  ([gs id f & args]
    (modify gs id #(apply f % args))))

(defn q
  ([gs k v]
    (-> gs :ave (get k) (get v) (or #{}))))

(defn setting
  ([gs k]
    (setting gs k nil))
  ([gs k default]
    (-> gs :settings (get k default))))