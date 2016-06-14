(ns falx.db.spec
  (:require [falx.db :as db]
            [clojure.spec :as s]
            [clojure.test.check.generators :as gen]))

(s/def ::db/id integer?)
(s/def ::db/key ::s/any)
(s/def ::db/value ::s/any)

(s/def ::db/entity
  (s/keys :req [::db/id]))

(s/def ::db/db
  (s/with-gen
    (s/keys :opt [::db/ave ::db/eav])
    #(gen/fmap
      db/db
      (s/gen (s/* ::db/entity)))))

(s/def ::db/ave
  (s/with-gen
    (s/map-of ::db/key (s/map-of ::db/value (s/coll-of ::db/id #{})))
    #(gen/fmap ::db/ave (s/gen ::db/db))))

(s/def ::db/eav
  (s/with-gen
    (s/map-of ::db/id ::db/entity)
    #(gen/fmap ::db/eav (s/gen ::db/db))))

(s/fdef db/exists?
  :args (s/cat :db ::db/db
               :id ::db/id)
  :ret boolean?)

(s/fdef db/assert
  :args (s/cat
          :db ::db/db
          :id ::db/id
          :key ::db/key
          :value ::db/value)
  :ret ::db/db
  :fn
  (s/and
    ;;idempotency
    (fn [{ret :ret {:keys [db id key value]} :args}]
      (= ret (db/assert db id key value)))))

(s/fdef db/entity
  :args (s/cat
          :db ::db/db
          :id ::db/id)
  :ret (s/nilable ::db/entity))

(s/fdef db/retract
  :args (s/cat
          :db ::db/db
          :id ::db/id
          :key ::db/key)
  :ret ::db/db)

(s/fdef db/delete
  :args (s/cat
          :db ::db/db
          :id ::db/id)
  :ret ::db/db)

(s/fdef db/add
  :args (s/cat
          :db ::db/db
          :entity ::db/entity)
  :ret ::db/db)

(s/fdef db/replace
  :args (s/cat
          :db ::db/db
          :entity ::db/entity)
  :ret ::db/db)

(s/fdef db/db
  :args (s/cat :ecoll (s/coll-of ::db/entity []))
  :ret ::db/db)

(s/fdef db/entity?
  :args (s/cat :x (s/or :entity ::db/entity
                        :else ::s/any))
  :ret boolean?
  :fn (s/and
        (fn [{ret :ret {[_ arg] :x} :args}]
          (if ret
            (s/valid? ::db/entity arg)
            (not (s/valid? ::db/entity arg))))))

(s/fdef db/iquery
  :args (s/tuple ::db/db ::db/key ::db/value)
  :ret (s/coll-of ::db/id #{}))

(s/fdef db/query
  :args (s/tuple ::db/db ::db/key ::db/value)
  :ret (s/coll-of ::db/entity #{}))
