(ns falx.game-state
  (:refer-clojure :exclude [assert])
  (:require [clojure.tools.logging :refer [info warn error]]
            [falx.point :as pt]
            [falx.util :as util]
            [clojure.set :as set]
            [falx.gdx :as gdx])
  (:import (java.util UUID)
           (java.io Writer)))

(alias 'c 'clojure.core)

(defrecord Settings [cell-size resolution binding])

(def default-settings
  (map->Settings
    {:cell-size  [64 64]
     :resolution {}
     :bindin     {:bind/click        {:hit :button/left}
                  :bind/alt-click    {:hit :button/right}
                  :bind/camera-fast  {:pressed #{:key/shift-left :key/shift-right}}
                  :bind/camera-up    {:pressed :key/w}
                  :bind/camera-left  {:pressed :key/a}
                  :bind/camera-down  {:pressed :key/s}
                  :bind/camera-right {:pressed :key/d}

                  :bind/quick-save   {:pressed #{:key/control-left :key/control-right}
                                      :hit     :key/s}}}))

(defrecord Input [hit pressed mouse])

(def empty-input
  (map->Input
    {:mouse   [0 0]
     :pressed #{}
     :hit     #{}}))

(let [button {0 :button/left
              1 :button/right
              2 :button/middle}
      key {}]
  (defn get-input
    []
    ))

(gdx/describe-button-code 1)
(gdx/describe-button-code 0)
(gdx/describe-button-code 2)

(defrecord GameState [eav ave schedule events requests input binds settings])

(defmethod print-method GameState
  [o ^Writer w]
  (.write w "#game-state [")
  (print-method {:entities (count (:eav o))} w)
  (.write w "]"))

(defn game-state?
  [x]
  (instance? GameState x))

(defn game-state
  ([]
   (map->GameState
     {:eav      {}
      :ave      {}
      :events   []
      :requests []
      :input    empty-input
      :binds #{}
      :settings default-settings})))

(defonce reactions (atom {}))

(defn pub
  ([gs event]
   (let [gs (update gs :events (fnil conj []) event)
         {:keys [:event/type]} event
         reactions (-> reactions :event (get type))]
     (reduce-kv (fn [gs k f] (or (f gs event) gs)) gs reactions)))
  ([gs event & more]
   (reduce gs (pub gs event) more)))

(defn do-defreaction
  [k event-type f]
  (swap!
    reactions
    (fn [m]
      (let [old (-> m :key (get k))]
        (-> m
            (util/dissoc-in [:event old k])
            (assoc-in [:event event-type k] f)
            (assoc-in [:key k] event-type)))))
  nil)

(defmacro defreaction
  [name event-type binding & body]
  (let [k (keyword (str *ns*) (str name))]
    `(do-defreaction ~k ~event-type (fn ~name ~binding ~@body))))

(defn request
  ([gs req])
  ([gs req & more]))

(defmulti respond (fn [gs req resp] (:request/type req)))

(defmulti do-command (fn [gs cmd] (:command/type cmd)))

(defmethod do-command :default
  [gs command]
  (warn "Unknown command" (:command/type command))
  {:gs gs})

(defmulti legal-command? (fn [gs cmd] (:command/type cmd)))

(defmethod legal-command? :default
  [_ _]
  true)

(defn legal?
  [gs command]
  (legal-command? gs command))

(defn command
  ([gs cmd]
   (if (legal? gs cmd)
     (or (do-command gs cmd) gs)
     gs))
  ([gs cmd & more]
   (reduce command (command gs cmd) more)))

;; Entities

(defrecord EntityId [uuid])

(defn eid?
  [x]
  (instance? EntityId x))

(defn entity?
  [x]
  (and (map? x) (eid? (:entity/id x))))

(defn generate-eid
  []
  (->EntityId (str (UUID/randomUUID))))

(defn exists?
  [gs eid]
  (let [{:keys [eav]} gs]
    (contains? eav eid)))

(defn entity
  [gs eid]
  (-> gs :eav (get eid)))

(defn all-eids
  [gs]
  (keys (:eav gs)))

(defn all-entities
  [gs]
  (map (:eav gs) (all-eids gs)))

(defn eid-of
  [gs x]
  (cond
    (eid? x) x
    (entity? x) (:entity/id (entity gs (:entity/id x)))))

(defn query
  ([gs k v]
   (or
     (-> gs :ave (get k) (get v))
     #{}))
  ([gs k v & kvs]
   (loop [set (query gs k v)
          kvs kvs]
     (if (empty? set)
       set
       (if-some [[k v & rest] (seq kvs)]
         (recur (set/intersection set (query gs k v))
                rest)
         set)))))

(defn where
  ([gs k]
   (query gs k true))
  ([gs k & ks]
   (loop [set (query gs k true)
          ks ks]
     (if (empty? set)
       set
       (if-some [[k & rest] (seq ks)]
         (recur (set/intersection set (query gs k true))
                rest)
         set)))))

(defn assert
  ([gs eid k v]
   (let [{:keys [eav ave]} gs
         e (-> eav (get eid))
         ne (assoc e k v)]
     (assoc gs
       :eav (assoc eav eid ne)
       :ave (-> ave
                (util/disjoc-in [k (get e k)] eid)
                (update-in [k v] (fnil conj #{}) eid)))))
  ([gs eid k v & kvs]
   (loop [gs (assert gs eid k v)
          kvs kvs]
     (if-some [[k v & rest] (seq kvs)]
       (recur (assert gs eid k v) rest)
       gs))))

(defn retract
  ([gs eid k]
   (let [{:keys [eav ave]} gs
         v (-> eav (get eid) (get k))]
     (assoc gs
       :eav (util/dissoc-in eav [eid k])
       :ave (util/disjoc-in ave [k v] eid))))
  ([gs eid k & ks]
   (loop [gs (retract gs eid k)
          ks ks]
     (if-some [[k & rest] (seq ks)]
       (recur (retract gs eid k) rest)
       gs))))

(defn attribute
  ([gs eid k]
   (-> gs :eav (get eid) (get k)))
  ([gs eid k not-found]
   (-> gs :eav (get eid) (get k not-found))))

(defn delete
  ([gs eid]
   (reduce (fn [gs k] (retract gs eid k)) gs (keys (entity gs eid))))
  ([gs eid & more]
   (reduce delete (delete gs eid) more)))

(defn add
  ([gs e]
   (let [{:keys [:entity/id]} e]
     (reduce-kv (fn [gs k v] (assert gs id k v)) gs e)))
  ([gs e & more]
   (reduce add (add gs e) more)))

;; Input

(defn handle-input
  [gs input]
  (let [{:keys [hit pressed]
         :or   {hit     #{}
                pressed #{}}} input
        {:keys [settings]
         previous-input :input} gs
        {:keys [binding]} settings
        bind-active? (fn bind-active? [bind]
                       (cond
                         (map? bind)
                         (let [{req-pressed :pressed
                                req-hit     :hit} bind]
                           (and
                             (cond
                               (keyword? req-pressed) (pressed req-pressed)
                               (empty? req-pressed) true
                               (set? req-pressed) (some pressed req-pressed)
                               (vector? req-pressed) (every? pressed req-pressed))
                             (cond
                               (keyword? req-hit) (hit req-hit)
                               (empty? req-hit) true
                               (set? req-hit) (some hit req-hit)
                               (vector? req-hit) (every? hit req-hit))))
                         (vector? bind)
                         (every? bind-active? bind)
                         (set? bind)
                         (some bind-active? bind)))
        binds (reduce-kv
                (fn [acc k bind]
                  (if (bind-active? bind)
                    (conj acc k)
                    acc))
                #{}
                binding)]
    (-> (assoc gs
          :input input
          :binds binds)
        (pub {:event/type :event.type/new-input
              :previous-input previous-input
              :input input
              :binds binds}))))

;; Locations

(defrecord Cell [area point])

(defn cell?
  [x]
  (instance? Cell x))

(defrecord Slice [area layer])

(defn slice?
  [x]
  (instance? Slice x))

(defn cell-of
  [gs x]
  (cond
    (cell? x) x
    (entity? x) (attribute gs (:entity/id x) :entity/cell)
    (eid? x) (attribute gs x :entity/cell)))

(defn point-of
  [gs x]
  (if (pt/point? x)
    x
    (:point (cell-of gs x))))

(defn screen-rect-of
  [gs x]
  (when-some [[x y] (point-of gs x)]
    (let [{:keys [settings]} gs
          {:keys [cell-size]
           :or   {cell-size [64 64]}} settings
          [w h] cell-size]
      [(* x w) (* y h) w h])))

;; Spawn

(defmethod legal-command? :command.type/spawn
  [gs {:keys [id]}]
  (or (nil? id)
      (not (exists? gs id))))

(defmethod do-command :command.type/spawn
  [gs {:keys [pos
              eid
              template]}]
  (let [eid (or eid (generate-eid))
        m template
        e (merge
            m
            {:entity/id eid}
            (when pos
              (let [cell (cell-of gs pos)
                    {:keys [area point]} cell
                    {:keys [:entity/layer]} m]
                {:entity/cell  cell
                 :entity/area  area
                 :entity/point point
                 :entity/slice (->Slice area layer)})))]
    (-> (add gs e)
        (pub {:event/type :event.type/spawned
              :eid eid}))))

;; Put

(defmethod do-command :command.type/put
  [gs {:keys [eid pos]}]
  (let [eid (eid-of gs eid)
        cell (cell-of gs pos)
        e (entity gs eid)
        {:keys [area point]} cell
        {:keys [:entity/layer] previous-cell :entity/cell} e]
    (-> (assert gs eid
                :entity/cell cell
                :entity/area area
                :entity/point point
                :entity/slice (->Slice area layer))
        (pub {:event/type :event.type/put
              :eid eid
              :previous-cell previous-cell
              :cell cell}))))
