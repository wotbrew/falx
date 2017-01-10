(ns falx.game-state
  (:refer-clojure :exclude [empty assert alter])
  (:require [falx.util :as util]
            [clojure.set :as set])
  (:import (java.util UUID)
           (clojure.lang IFn PersistentQueue)
           (com.badlogic.gdx Input$Buttons)))

;; requests
{:reqid #uuid "balbla"
 :type     :spawn
 :template :fred
 :cell {:level :testing
        :point [3 4]}
 ;; optional
 :add      {}
 ;; optional
 :id       0}

{:type :step
 :id 0
 :direction :left}

{:type :click
 :point [3 4]}

(comment
  ;; request, returns {:request, :response, :events} response
  (request gs req)
  ;; apply event to return a new state
  (apply-event gs event)
  ;; apply response
  (apply-response gs resp)

  ;; send request and ignore responses, applying events immediately
  (submit gs req)

  ;; a request for a side effect from the engine
  (perform gs effect))

;; events
{:type :spawned
 :success? true
 :template :fred
 :add {}
 :id 0
 :entity {}}

{:type :put
 :success? true
 :id 0
 :old  {:level :testing
        :point [3 4]}
 :new  {:level :testing
        :point [5 6]}}

(def empty
  {:entity   {}
   :ave      {}
   :id-seed  0
   :camera [0 0]
   :settings {:aspect-ratio 4/3
              :resolution       [640 480]
              :cell-size        [64 64]
              :click-button     Input$Buttons/LEFT
              :alt-click-button Input$Buttons/RIGHT}})

(def settings :settings)

(def aspect-ratio (util/lens settings :aspect-ratio))
(def set-aspect-ratio (util/setter aspect-ratio))

(def resolution (util/lens settings :resolution))
(def set-resolution (util/setter resolution))

(def cell-size (util/lens settings :cell-size))
(def set-cell-size (util/setter cell-size))
(def update-cell-size (util/updater cell-size))

(def click-button (util/lens settings :click-button))
(def alt-click-button (util/lens settings :alt-click-button))

(def camera :camera)
(def set-camera (util/setter camera))
(def update-camera (util/updater camera))

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
  [gs event]
  (-> (update gs :events (fnil conj []) event)
      (apply-event event)))

(defn entity
  [gs id]
  (-> gs :entity (get id)))

(defn query
  ([gs k v]
   (-> gs :ave (get k) (get v) (or #{})))
  ([gs k v & kvs]
   (loop [ret (query gs k v)
          kvs kvs]
     (if-some [[k v & kvs] (seq kvs)]
       (recur (set/intersection ret (query gs k v)) kvs)
       ret))))

(defn equery
  ([gs k v]
   (map (:entity gs) (query gs k v)))
  ([gs k v & kvs]
   (map (:entity gs) (apply query gs k v kvs))))

(defn with
  ([gs k]
   (query gs k true))
  ([gs k & ks]
   (apply query gs k (mapcat vector ks (repeat true)))))

(defn ewith
  ([gs k]
   (equery gs k true))
  ([gs k & ks]
   (apply equery gs k (mapcat vector ks (repeat true)))))

(defn retract
  [gs id k]
  (-> gs
      (update :entity util/dissoc-in [id k])
      (update :ave util/disjoc-in [k (get (entity gs id) k)] id)))

(defn assert
  [gs id k v]
  (let [gs (retract gs id k)]
    (-> gs
        (assoc-in [:entity id k] v)
        (assoc-in [:entity id :id] id)
        (update-in [:ave k v] (fnil conj #{}) id))))

(defn alter
  ([gs id k f]
   (let [ev (get (entity gs id) k)]
     (assert gs id k (f ev))))
  ([gs id k f & args]
   (alter gs id k #(apply f % args))))

(defn next-id
  [gs]
  [(inc (:id-seed gs 0))
   (update gs :id-seed (fnil inc 0))])

(defn add
  [gs e]
  (if-some [id (:id e)]
    (reduce-kv #(assert %1 id %2 %3) gs e)
    (let [[id gs] (next-id gs)]
      (recur gs (assoc e :id id)))))

(defn alter-entity
  ([gs id f]
   (add gs (f (assoc (entity gs id) :id id))))
  ([gs id f & args]
   (alter-entity gs id #(apply f % args))))

(defn retract-entity
  ([gs id]
   (reduce-kv (fn [gs k _] (retract gs id k)) gs (entity gs id))))

(defn replace-entity
  ([gs e]
   (if-some [id (:id e)]
     (-> (retract-entity gs id)
         (add e))
     (add gs e))))

(defn tempid
  ([]
   (UUID/randomUUID)))

(defn transact
  [gs tx-data]
  (reduce
    (fn ! [db tx]
      (if (map? tx)
        (add db tx)
        (let [f (first tx)
              args (rest tx)]
          (apply f db args))))
    gs
    tx-data))

(defn center-camera-on-pt
  ([gs pt]
   (let [[x y] pt]
     (center-camera-on-pt gs x y)))
  ([gs x y]
   (let [[cw ch] (cell-size gs)
         [sw sh] (resolution gs)]
     (set-camera gs [(double (+ (/ sw 2) (* x (- cw)) (/ (- cw) 2)))
                     (double (+ (/ sh 2) (* y (- ch)) (/ (- ch) 2)))]))))

(defn center-camera-on
  [gs id]
  (if-some [pt (:pt (entity gs id))]
    (center-camera-on-pt gs pt)
    gs))

(def active-party :active-party)
(def active-party-entity
  (fn [gs]
    (entity gs (:active-party gs))))

(defn center-camera-on-active-party
  [gs]
  (center-camera-on gs (active-party gs)))

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

(defn corpse-of
  [e]
  (let [cell (:cell e)]
    {:type   :corpse
     :cell   cell
     :pt     (:pt cell)
     :offset (let [x (+ (rand 2) -1)
                   y (+ (rand 2) -1)]
               [x y])
     :slice  {:level (:level cell)
              :layer :corpse}
     :level  (:level cell)
     :layer  :corpse}))

(defn melee-attack
  [gs attacker defender]
  (let [aparty (entity gs attacker)
        dparty (entity gs defender)
        aents (equery gs :party attacker)
        dents (equery gs :party defender)]
    (if (seq dents)
      (-> gs
          (retract-entity (:id (rand-nth (vec dents))))
          (add (corpse-of dparty))
          (cond->
            (empty? (rest dents)) (retract-entity defender)))
      gs)))

(defn turn-of?
  [gs id]
  true
  #_
  (= id (peek (:turn-queue gs))))

(defn end-turn
  [gs]
  (update gs :turn-queue (fnil pop PersistentQueue/EMPTY)))

(defn- move-party*
  [gs id cell]
  (if (solid? gs cell)
    (if-some [opposing-party (party-at gs cell)]
      (melee-attack gs id opposing-party)
      gs)
    (let [{:keys [level pt]} cell]
      (add
        gs
        {:id    id
         :cell  cell
         :level level
         :pt    pt
         :layer :creature
         :slice {:layer :creature
                 :level level}}))))

(defn move-party
  [gs id cell]
  (if (turn-of? gs id)
    (-> (move-party* gs id cell) end-turn)
    gs))

(defn move-active-party
  [gs direction]
  (if (= direction [0 0])
    gs
    (if-some [e (active-party-entity gs)]
      (if-some [{:keys [level pt]} (:cell e)]
        (-> (move-party gs (:id e) {:level level
                                    :pt    (mapv + pt direction)})
            center-camera-on-active-party)
        gs)
      gs)))

(defn wait
  [gs seconds]
  (assoc gs :wait-seconds seconds
            :waiting-for 0.0))

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
    :else gs))