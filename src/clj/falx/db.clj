(ns falx.db
  (:require [clojure.set :as set]
            [falx.util :as util]
            [clojure.core.async :as async])
  (:import (java.util HashMap)
           (java.util.concurrent.atomic AtomicLong)
           (clojure.lang IDeref ILookup)
           (java.io Writer)))

(defrecord DB [eav ave schema next-eid]
  Object
  (toString [this]
    "#falx.db.DB []"))

(defrecord Eid [x])

(defn eid?
  [x]
  (instance? Eid x))

(defn index?
  [db a]
  (-> db :schema (get a) :index?))

(defmethod print-method DB
  [o ^Writer writer]
  (.write writer (str o)))

(defn entity-map
  [db eid]
  (-> db :eav (get eid)))

(defn q
  ([db k v]
   (-> db :ave (get k) (get v) (or #{})))
  ([db k v & kvs]
   (loop [set (q db k v)
          kvs kvs]
     (if (= set #{})
       #{}
       (if-some [[k v & rest] (seq kvs)]
         (recur (set/intersection set (q db k v)) rest)
         set)))))

(defn attr
  ([db eid k]
   (-> db
       :eav
       (get eid)
       (get k)))
  ([db eid k not-found]
   (-> db
       :eav
       (get eid)
       (get k not-found))))

(defn set-attr
  ([db eid k v]
   (let [eav (:eav db)
         ave (:ave db)]
     (->DB (assoc-in eav [eid k] v)
           (if (index? db k)
             (-> (let [old (attr db eid k ::not-found)]
                   (if (identical? old ::not-found)
                     ave
                     (util/disjoc-in ave [k old] eid)))
                 (update-in [k v] (fnil conj #{}) eid))
             ave)
           (:schema db)
           (:eid-seed db))))
  ([db eid k v & kvs]
   (as-> db db
         (set-attr db eid k v)
         (reduce (fn [db [k v]] (set-attr db eid k v)) db (partition 2 kvs)))))

(defn alter-attr
  ([db eid k f]
    (let [ev (attr db eid k)]
      (set-attr db eid k (f ev))))
  ([db eid k f & args]
    (alter-attr db eid k #(apply f % args))))

(defn merge-attr
  ([db eid m]
   (if (nil? m)
     db
     (reduce-kv (fn [db k v] (set-attr db eid k v)) db m)))
  ([db eid m & more]
   (reduce #(merge-attr %1 eid %2) (merge-attr db eid m) more)))

(defn delete-attr
  ([db eid k]
   (let [eav (:eav db)
         ave (:ave db)]
     (->DB
       (util/dissoc-in eav [eid k])
       (if (index? db k)
         (let [old (attr db eid k ::not-found)]
           (if (identical? old ::not-found)
             ave
             (util/disjoc-in ave [k old] eid)))
         ave)
       (:schema db)
       (:eid-seed db))))
  ([db eid k & ks]
   (reduce #(delete-attr %1 eid %2) (delete-attr db eid k) ks)))

(defn delete
  ([db eid]
   (if-some [e (entity-map db eid)]
     (let [eav (:eav db)
           ave (:ave db)]
       (assoc db
         :eav (dissoc eav eid)
         :ave (reduce-kv #(util/disjoc-in %1 [%2 %3] eid) ave e)))
     db))
  ([db eid & more]
   (reduce delete (delete db eid) more)))

(defn add
  [db eid m]
  (reduce-kv #(set-attr %1 eid %2 %3) db m))

(defn set-entity
  [db eid m]
  (-> (delete db eid)
      (add eid m)))

(defn modify
  ([db eid f]
   (let [e (entity-map db eid)]
     (set-entity db eid (f e))))
  ([db eid f & args]
   (modify db eid #(apply f % args))))

(defrecord TempId [n])

(def ^:private tempid-seed
  (atom 0))

(defn tempid
  ([]
   (->TempId (swap! tempid-seed dec)))
  ([n]
   (swap! tempid-seed min n)
   (->TempId n)))

(defn tempid?
  [x]
  (instance? TempId x))

(declare eid)

(defmulti tx-op
  (fn [db op resolve]
    (first op)))

(defmethod tx-op :default
  [db op resolve]
  (throw (IllegalArgumentException.
           (str "Not valid tx function " op))))

(defn transact-result
  [db tx]
  (let [idm (HashMap.)
        ids (AtomicLong. (:next-eid db 0))
        resolve (fn [x]
                  (cond
                    (tempid? x) (.get idm x)
                    (eid? x) x
                    :else
                    (let [n (.getAndIncrement ids)
                          eid (->Eid n)]
                      (.put idm x eid)
                      eid)))]
    {:resolve-tempid (fn [x] (.get idm x))
     :db-before      db
     :db-after       (->
                       (reduce
                         (fn ! [db t]
                           (cond
                             (vector? t)
                             (case (nth t 0)
                               :db/set (set-attr db (resolve (nth t 1)) (nth t 2) (nth t 3))
                               :db/merge (merge-attr db (resolve (nth t 1)) (nth t 2))
                               :db/delete (delete-attr db (resolve (nth t 1)) (nth t 2))
                               :db/delete-entity (delete db (resolve nth t 1))
                               :db/fn (if (= 2 (count t))
                                        ((nth t 1) db)
                                        (apply (nth t 1) (subvec t 2)))
                               :db/modify (if (= 3 (count t))
                                            (modify db (nth t 1) (nth t 2))
                                            (apply modify db (nth t 1) (subvec t 2)))
                               :db/apply
                               (let [eid (resolve (nth t 1))
                                     k (nth t 2)
                                     ev (attr db eid k)]
                                 (set-attr db eid k
                                           (if (= 3 (count t))
                                             ((nth t 2) ev)
                                             (apply (nth t 2) ev (subvec t 3)))))
                               (reduce ! db (tx-op db t resolve)))
                             (map? t)
                             (let [eid (resolve (:db/id t))
                                   t (assoc t :db/id eid)]
                               (reduce-kv (fn [db k v]
                                            (set-attr db eid k v))
                                          db t))

                             :else (throw (IllegalArgumentException. "Not valid tx data"))))
                         db
                         tx)
                       (assoc :next-eid (.get ids)))}))

(defn transact
  [db tx]
  (:db-after (transact-result db tx)))

(defn conn
  ([db]
   (agent db
          :error-handler (fn [a exc]
                           (println exc)))))

(defn transact!
  [conn tx]
  (let [p (promise)]
    (send conn (fn [db]
                 (let [result (transact-result db tx)]
                   (deliver p result)
                   (:db-after result))))
    p))

(defn transact-async!
  [conn tx]
  (let [c (async/promise-chan)]
    (send conn (fn [db]
                 (let [result (transact-result db tx)]
                   (async/>!! c tx)
                   (:db-after result))))
    c))

(defn resolve-tempid
  [tx-result tempid]
  ((:resolve-tempid tx-result) tempid))

(defn db
  ([schema]
   (map->DB
     {:eav      {}
      :ave      {}
      :schema   (merge
                  {:db/ident {:index? true}}
                  schema)
      :idents   {}
      :next-eid 0})))

(deftype Entity [db eid]
  IDeref
  (deref [this]
    (entity-map db eid))
  ILookup
  (valAt [this k]
    (attr db eid k))
  (valAt [this k not-found]
    (attr db eid k not-found)))

(deftype ERef [conn eid pending]
  IDeref
  (deref [this]
    (if pending
      @pending
      (entity-map @conn eid))))

(defn eid
  [x]
  (condp instance? x
    Entity (.-eid ^Entity x)
    ERef (.-eid ^ERef x)
    x))

(defn edb
  [e-or-eref]
  (if (instance? Entity e-or-eref)
    (:db e-or-eref)
    @(.-conn ^ERef e-or-eref)))

(defn entity
  ([e]
    (condp instance? e
      Entity e
      ERef (entity (edb e) (eid e))
      (throw (IllegalArgumentException. (str "Not able to return entity of " (type e))))))
  ([db e]
   (condp instance? e
     Entity (if (identical? (edb e) db)
              e
              (->Entity db (eid e)))
     ERef (entity (edb e) (eid e))
     (->Entity db (eid e)))))

(defn eref
  [conn e]
  (->ERef conn (eid e) nil))

(defn modify!
  ([^ERef eref f]
   (let [p (promise)]
     (send (.-conn eref) modify (.-eid eref)
           (fn [e]
             (let [ret (f e)]
               (deliver p ret)
               ret)))
     (->ERef (.-conn eref) (.-eid eref) p)))
  ([^ERef eref f & args]
    (modify! eref #(apply f % args))))

(defn overwrite!
  [eref v]
  (modify! eref (constantly v)))
