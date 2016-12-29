(ns falx.util
  (:require [clojure.pprint :refer [pprint]]))

(defn dissoc-in
  "Dissociate a value in a nested assocative structure, identified by a sequence
  of keys. Any collections left empty by the operation will be dissociated from
  their containing structures."
  [m ks]
  (if-let [[k & ks] (seq ks)]
    (if (seq ks)
      (let [v (dissoc-in (get m k) ks)]
        (if (empty? v)
          (dissoc m k)
          (assoc m k v)))
      (dissoc m k))
    m))

(defn entity
  [gs id]
  (-> gs :entities (get id)))

(defn pprint-str
  [x]
  (with-out-str
    (pprint x)))