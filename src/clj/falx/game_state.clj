(ns falx.game-state
  (:refer-clojure :exclude [empty])
  (:require [falx.util :as util]
            [falx.db :as db]
            [clojure.set :as set]))

(defrecord GameState [db scene scene-stack active-party players camera settings events])

(def empty
  (->GameState :main-menu [:main-menu] nil [] db/empty [0 0] {} []))

(defonce event-handler-registry
  (atom {}))

(defmacro defhandler
  [type k f]
  `(swap! event-handler-registry assoc-in [~type ~k] ~f))

(let [f (fn [gs _ f] (f gs))]
  (defn- apply-event
    [gs event]
    (let [t (:type event)
          hm (get @event-handler-registry t)]
      (reduce-kv f gs hm))))

(defn publish
  ([gs event]
   (-> (update gs :events conj event)
       (apply-event event)))
  ([gs event & more]
   (reduce publish gs (cons event more))))

(defn transact*
  [gs tx-data]
  (let [{:keys [db]} gs
        {:keys [db-after tempids]} (db/transact db tx-data)]
    {:gs-before gs
     :gs-after (assoc gs :db db-after)
     :tempids tempids}))

(defn transact
  [gs tx-data]
  (let [{:keys [db]} gs
        {:keys [db-after tempids]} (db/transact db tx-data)]
    (assoc gs :db db-after)))

(defn set-setting
  [gs k v]
  (assoc-in gs [:settings k] v))

(defonce setting-defaults
  (atom {}))

(defn setting
  ([gs k]
   (setting gs k (or (get @setting-defaults k))))
  ([gs k default]
   (-> gs :settings (get k default))))

(defn update-setting
  ([gs k f]
   (set-setting gs k (f (setting gs k))))
  ([gs k f & args]
   (update-setting gs k #(apply f % args))))

(defmacro defsetting
  [k v]
  `(do
     (swap! setting-defaults assoc ~k ~v)
     nil))

(defn center-camera
  ([gs pt]
   (let [[x y] pt]
     (center-camera gs x y)))
  ([gs x y]
   (let [[cw ch] (setting gs :cell-size)
         [sw sh] (setting gs :resolution)]
     (assoc gs :camera [(double (+ (/ sw 2) (* x (- cw))))
                        (double (+ (/ sh 2) (* y (- ch))))]))))

(defn entity
  [gs id]
  (db/entity (:db gs) id))

(defn equery
  [gs k v]
  (db/equery (:db gs) k v))

(defn query
  ([gs k v]
   (db/query (:db gs) k v))
  ([gs k v & kvs]
    (apply db/query (:db gs) k v kvs)))

(defn del
  [gs id]
  (update
    gs
    :db
    (comp :db-after db/transact)
    [[:db/retract-entity id]]))

(defn add
  [gs id m]
  (update
    gs
    :db
    (comp :db-after db/transact)
    [(assoc m :db/id id)]))

(defn spawn
  [gs m]
  (add gs (db/tempid) m))

(let [tmpidseed (atom 0)]
  (defn next-id
    [gs]
    [(swap! tmpidseed dec) gs]))

(defn modify
  ([gs id f]
   (update
     gs
     :db
     (comp :db-after db/transact)
     [[:db/update-entity id f]]))
  ([gs id f & args]
    (modify gs id #(apply f % args))))

(defn active-party
  [gs]
  (entity gs (:active-party gs)))

(defn center-camera-on-active-party
  ([gs]
   (if-some [pt (:pt (active-party gs))]
     (center-camera gs pt)
     gs)))

(defn solid?
  [gs cell]
  (some :solid? (equery gs :cell cell)))

(defn party?
  ([e]
   (= (:type e) :party))
  ([gs id]
   (party? (entity gs id))))

(defn party-at
  [gs cell]
  (first (filter (partial party? gs) (query gs :cell cell))))

(defn attack
  [gs attacker defender]
  (let [aparty (entity gs attacker)
        dparty (entity gs defender)
        aents (equery gs :party attacker)
        dents (equery gs :party defender)
        cell (:cell dparty)]
    (if (seq dents)
      (-> (del gs (:db/id (rand-nth (vec dents))))
          (spawn {:type :corpse
                  :cell cell
                  :pt (:pt cell)
                  :offset [(rand) (rand)]
                  :slice {:level (:level cell)
                          :layer :corpse}
                  :level (:level cell)
                  :layer :corpse}))
      (del gs defender))))

(defn move-party
  [gs id cell]
  (->
    (if (solid? gs cell)
      (if-some [opposing-party (party-at gs cell)]
        (attack gs id opposing-party)
        gs)
      (let [{:keys [level pt]} cell]
        (modify
          gs id
          assoc
          :cell cell
          :level level
          :pt pt
          :layer :creature
          :slice {:layer :creature
                  :level level})))
    (assoc :ai-turn? true)))

(defn move-active-party
  [gs direction]
  (if (= direction [0 0])
    gs
    (if-some [e (active-party gs)]
      (if-some [{:keys [level pt]} (:cell e)]
        (cond-> (move-party gs (:db/id e) {:level level
                                        :pt    (mapv + pt direction)})
                (:player-party? e) center-camera-on-active-party)
        gs)
      gs)))

(defn wait
  [gs seconds]
  (assoc gs :wait-seconds seconds
            :waiting-for 0.0))

(defn play-ai-turn
  [gs]
  (if-some [id (peek (:ai-turn-sequence gs))]
    (-> (update gs :ai-turn-sequence pop)
        (wait 0.3))
    (dissoc gs
            :ai-turn-sequence
            :ai-turn?)))

(defn create-turn-sequence
  [gs]
  (assoc gs :ai-turn-sequence (vec (query gs :enemy? true :type :party))))

(defn await-timeout
  [gs seconds delta]
  (if-some [waiting-for (:waiting-for gs)]
    (if (< (+ waiting-for delta) seconds)
      (assoc gs :waiting-for (+ waiting-for delta))
      (dissoc gs :waiting-for :wait-seconds))
    gs))

(defn simulate
  [gs delta]
  (cond
    (:wait-seconds gs) (await-timeout gs (:wait-seconds gs) delta)
    (:ai-turn-sequence gs) (play-ai-turn gs)
    (:ai-turn? gs) (create-turn-sequence gs)
    :else gs))