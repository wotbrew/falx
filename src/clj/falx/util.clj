(ns falx.util
  (:require [clojure.pprint :refer [pprint]])
  (:import (clojure.lang Keyword AFn IFn Sequential)))

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

(defn pprint-str
  [x]
  (with-out-str
    (pprint x)))

(defprotocol ILens
  (-lget [this x])
  (-lset [this x v]))

(defn lget
  [x lens]
  (-lget lens x))

(defn lset
  [x lens v]
  (-lset lens x v))

(defn lupdate
  ([x lens f]
   (lset x lens (f (lget x lens))))
  ([x lens f & args]
   (lupdate x lens #(apply f % args))))

(defn setter
  [lens]
  (fn [x v]
    (lset x lens v)))

(defn updater
  [lens]
  (fn
    ([x f]
     (lupdate x lens f))
    ([x f & args]
     (lupdate x lens #(apply f % args)))))

(defn lens
  [lens & lenses]
  (if (seq lenses)
    (let [lenses (into [lens] lenses)]
      (reify ILens
        (-lget [this x]
          (reduce lget x lenses))
        (-lset [this x v]
          ((fn ! [x lenses v]
             (let [[lense & rest] (seq lenses)]
               (if rest
                 (-lset lense x (! (-lget lense x) rest v))
                 (-lset lense x v))))
            x lenses v))
        IFn
        (invoke [this x]
          (-lget this x))))
    lens))

(extend-protocol ILens
  AFn
  (-lget [this x]
    (this x))
  (-lset [this x v]
    (throw (Exception. "Not allowed to set a fn lens")))
  Keyword
  (-lget [this x]
    (this x))
  (-lset [this x v]
    (assoc x this v))
  Sequential
  (-lget [this x]
    (reduce lget x this))
  (-lset [this x v]
    ((fn ! [x lenses v]
       (let [[lense & rest] (seq lenses)]
         (if rest
           (-lset lense x (! (-lget lense x) rest v))
           (-lset lense x v))))
      x this v))
  IFn
  (invoke [this x]
    (-lget this x)))
