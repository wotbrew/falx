(ns falx.types
  (:require [falx.db :as db])
  (:import (clojure.lang IDeref)))

(deftype Game [db-ref user-ref id-atom])

(deftype EntityRef [id db-ref]
  IDeref
  (deref [this]
    (-> @db-ref (db/entity id))))

(prefer-method print-method IDeref EntityRef)
(prefer-method print-dup IDeref EntityRef)
