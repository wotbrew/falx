(ns falx.space
  (:import (java.util UUID)))

(defn disjoc
  [m k v]
  (let [s (get m k)]
    (if-some [new (not-empty (disj s v))]
      (assoc m k new)
      (dissoc m k))))

(defn disjoc-in
  [m [k & ks] v]
  (if (seq ks)
    (if-some [m2 (not-empty (disjoc-in (get m k) ks v))]
      (assoc m k m2)
      (dissoc m k))
    (disjoc m k v)))

(defn dissoc-in
  [m [k & ks]]
  (if (seq ks)
    (if-some [m2 (not-empty (dissoc-in (get m k) ks))]
      (assoc m k m2)
      (dissoc m k))
    (dissoc m k)))

(defn space
  [name]
  {:id   (str (UUID/randomUUID))
   :name name
   :idp {}
   :pid {}
   :sorted {}})

(defn find
  [space id]
  (-> space :idp (get id)))

(defn unput
  [space id]
  (if-let [current (find space id)]
    (-> (dissoc-in space [:idp id])
        (disjoc-in [:pid current] id)
        (dissoc-in [:sorted (:map current)]))))

(defn put
  [space id position])

(defn in-map
  [space map])