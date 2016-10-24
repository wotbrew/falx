(ns falx.entity
  (:refer-clojure :excludes [isa?])
  (:require [falx.db :as db]))

(def ^:private -isa? clojure.core/isa?)

(def schema
  {::kind {}})

(defn kind
  [db eid]
  (db/attr db eid ::kind))

(defn isa?
  [db eid k]
  (-isa? (kind db eid) k))