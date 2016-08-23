(ns falx.game
  (:require [falx.db :as db]
            [falx.types :as types])
  (:import (falx.types Game EntityRef)))

(defn game
  []
  (types/->Game (ref {}) (ref {}) (atom 0)))

(defn db-ref
  [^Game g]
  (.-db-ref g))

(defn db-alter
  ([g f]
   (alter (db-ref g) f))
  ([eref f & args]
   (db-alter eref #(apply f % args))))

(defn user-ref
  [^Game g]
  (.-user-ref g))

(defn user-alter
  ([g f]
   (alter (user-ref g) f))
  ([g f & args]
   (user-alter g #(apply f % args))))

(defn id-atom
  [^Game g]
  (.-id-atom g))

(defn new-id
  [g]
  (swap! (id-atom g) inc))

(defn eref
  [g id]
  (types/->EntityRef id (db-ref g)))

(defn eref-alter
  ([^EntityRef eref f]
   (-> (alter (.-db-ref eref) db/alter (.id eref) f)
       (db/entity (.id eref))))
  ([eref f & args]
   (eref-alter eref #(apply f % args))))

(defn eref-create
  [g m]
  (let [id (new-id g)
        m (assoc m ::db/id id)]
    (db-alter g db/add m)
    (eref g id)))

(defn id
  [^EntityRef eref]
  (.id eref))
