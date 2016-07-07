(ns falx.db-test
  (:require [falx.db :as db]
            [falx.db.spec]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.spec :as s]
            [clojure.spec.test :as st]))

(deftest specs-pass?
  (is (every? (complement :failure)
              (st/check (st/enumerate-namespace 'falx.db)
                        {:clojure.spec.test.check/opts {:num-tests 100}}))))

(deftest entity?-example
  (is (db/entity? {::db/id 0}))
  (is (not (db/entity? {})))
  (is (not (db/entity? {:foo :bar})))
  (is (not (db/entity? nil)))
  (is (not (db/entity? "foo"))))

(deftest assert-example
  (is (= (db/assert {} 0 :foo :bar)
         (db/db [{::db/id 0
                  :foo :bar}]))))

(defspec value-asserted-is-added-to-entity
  (prop/for-all
    [db (s/gen ::db/db)
     id (s/gen ::db/id)
     k (s/gen ::db/key)
     v (s/gen ::db/value)]
    (-> (db/assert db id k v)
        (db/entity id)
        (get k)
        (= v))))

(comment
  (value-asserted-is-added-to-entity))

(defspec assert-is-idempotent
  (prop/for-all
    [db (s/gen ::db/db)
     id (s/gen ::db/id)
     k (s/gen ::db/key)
     v (s/gen ::db/value)]
    (-> (db/assert db id k v)
        (db/assert id k v)
        (= (db/assert db id k v)))))

(comment
  (assert-is-idempotent))

(defspec assert-creates-entity-if-not-existing
  (prop/for-all
    [db (s/gen ::db/db)
     id (s/gen ::db/id)
     k (s/gen ::db/key)
     v (s/gen ::db/value)]
    (-> (db/delete db id)
        (db/assert id k v)
        (db/entity id)
        (get k)
        (= v))))

(comment
  (assert-creates-entity-if-not-existing))

(defspec deleted-entities-no-longer-exist
  (prop/for-all
    [db (s/gen ::db/db)
     id (s/gen ::db/id)]
    (-> (db/delete db id)
        (db/exists? id)
        not)))

(comment
  (deleted-entities-no-longer-exist))

(defspec delete-is-idempotent
  (prop/for-all
    [db (s/gen ::db/db)
     id (s/gen ::db/id)]
    (-> (db/delete db id)
        (db/delete id)
        (= (db/delete db id)))))

(comment
  (assert-is-idempotent))

(defspec entities-exist
  (prop/for-all
    [db (s/gen ::db/db)
     id (s/gen ::db/id)]
    (if (some? (db/entity db id))
      (db/exists? db id)
      (not (db/exists? db id)))))

(comment
  (entities-exist))

(defspec all-ids-exist
  (prop/for-all
    [db (s/gen ::db/db)]
    (every? #(db/exists? db %) (db/ids db))))

(comment
  (all-ids-exist))