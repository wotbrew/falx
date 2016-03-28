(ns falx.game
  (:require [falx.protocol :as p]
            [falx.event :as event]
            [falx.util :as util]
            [clojure.set :as set]
            [falx.frame :as frame]))

(defn add-subm
  ([g subm]
   (update g :subs #(merge-with (fnil into []) % subm)))
  ([g subm & more]
   (reduce add-subm g (cons subm more))))

(defn add-sub
  ([g kind sub]
   (update-in g [:subs kind] (fnil conj []) sub))
  ([g kind sub & more]
   (reduce #(add-sub %1 kind %2) g (cons sub more))))

(defn get-subs
  [g kind]
  (-> g :subs (get kind)))

(defn run-subs
  ([g kind]
   (let [subs (get-subs g kind)]
     (reduce #(%2 %1) g subs)))
  ([g kind x]
   (let [subs (get-subs g kind)]
     (reduce #(%2 %1 x) g subs)))
  ([g kind x y]
   (let [subs (get-subs g kind)]
     (reduce #(%2 %1 x y) g subs)))
  ([g kind x y & args]
   (let [subs (get-subs g kind)]
     (reduce #(apply %2 %1 x y args) g subs))))

(defn publish
  ([g event]
   (-> (update g :events (fnil conj []) event)
       (run-subs :event event)))
  ([g event & more]
   (reduce publish g (cons event more))))

(defn perform
  ([g] g)
  ([g action]
   (-> (p/-perform action g)
       (run-subs :action)))
  ([g action & actions]
   (reduce perform g (cons action actions))))

(defn in-time
  [g offset-secs]
  (+ (:time g 0.0) offset-secs))

(defn schedule
  [g action time]
  (update g :scheduled (fnil update (sorted-map)) time (fnil conj []) action))

(defn schedule-in
  [g action in-secs]
  (schedule g action (in-time g in-secs)))

(defn run-scheduled-actions
  ([g]
   (run-scheduled-actions g (:time g 0.0)))
  ([g time]
   (if (nil? (:scheduled g))
     g
     (let [kvs (subseq (:scheduled g) <= time)]
       (-> (transduce (mapcat val) perform g kvs)
           (update :scheduled #(transduce (map key) dissoc % kvs)))))))

(defn request
  ([g req]
   (-> (update g :requests (fnil conj []) req)
       (run-subs :requests req)))
  ([g req & more]
   (reduce request g (cons req more))))

(defn respond
  [g req response]
  (-> (p/-respond req g response)
      (run-subs :response req response)))

(defn get-actor
  [g id]
  (-> g :eav (get id)))

(defn get-attr
  ([g id k]
   (-> g :eav (get id) (get k)))
  ([g id k not-found]
   (-> g :eav (get id) (get k not-found))))

(defn rem-attr
  ([g id k]
   (let [v (get-attr g id k)]
     (-> g
         (util/dissoc-in [:eav id k])
         (util/disjoc-in [:ave k v] id))))
  ([g id k & ks]
   (reduce #(rem-attr %1 id %2) g (cons k ks))))

(defn set-attr
  ([g id k v]
   (-> g
       (assoc-in [:eav id k] v)
       (update-in [:ave k v] util/set-conj id)))
  ([g id k v & kvs]
   (->> (cons [k v] (partition 2 kvs))
        (reduce #(set-attr %1 id (first %2) (second %2)) g))))

(defn rem-actor
  [g id]
  (let [ks (keys (get-actor g id))]
    (reduce #(rem-attr %1 id %2) g ks)))

(defn merge-actor
  ([g a]
   (merge-actor g (:id a) a))
  ([g id m]
   (reduce-kv #(set-attr %1 id %2 %3) g m))
  ([g id m & more]
   (reduce #(merge-actor %1 id %2) g (cons m more))))

(defn add-actor
  ([g a]
   (add-actor g (:id a) a))
  ([g id m]
   (-> (rem-actor g id)
       (merge-actor id m))))

(defn update-actor
  ([g id f]
   (if-some [a (get-actor g id)]
     (add-actor g (f a))
     g))
  ([g id f & args]
   (update-actor g id #(apply f % args))))

(defn iquery
  ([g m]
   (reduce-kv #(set/intersection %1 (iquery g %2 %3)) #{} m))
  ([g k v]
   (-> g :ave (get k) (get v #{})))
  ([g k v & kvs]
   (iquery g (into {k v} (partition 2 kvs)))))

(defn query
  ([g m]
   (map #(get-actor g %) (iquery g m)))
  ([g k v]
   (map #(get-actor g %) (iquery g k v)))
  ([g k v & kvs]
   (query g (into {k v} (partition 2 kvs)))))

(defn simulate
  [g frame]
  (-> (update g :time (fnil + 0.0) (frame/get-delta frame))
      (run-subs :sim)))

(defn set-display
  [g display]
  (if (= display (:display g))
    g
    (-> (assoc g :display display)
        (publish (event/display-changed (:display g) display)))))

(defn set-display-from-frame
  [g frame]
  (set-display g (:display frame)))

(defn set-input
  [g input]
  (if (not= input (:input g))
    (-> (assoc g :input input)
        (publish (event/input-changed (:input g) input)))
    g))

(def default-settings
  {:cell-size [32 32]})

(defn get-setting
  ([g k]
   (-> g :settings (get k)))
  ([g k not-found]
   (-> g :settings (get k not-found))))

(defn set-setting
  ([g k v]
   (let [ov (get-setting g k ::not-found)]
     (if (= ov v)
       g
       (-> (assoc-in g [:settings k] v)
           (publish (event/setting-changed k ov v))))))
  ([g k v & kvs]
   (reduce #(set-setting %1 (first %2) (second %2)) g (cons [k v] (partition 2 kvs)))))

(defmulti default-event-handler (fn [g event] (:type event)))

(defmethod default-event-handler :default
  [g event]
  g)

(defmethod default-event-handler :event/multi
  [g event]
  (reduce publish g (:events event)))

(defn run-event-subs
  [g event]
  (run-subs g (:type event) event))

(def default-subm
  {:frame [#'simulate
           #'set-display-from-frame]
   :input [#'set-input]
   :sim   [#'run-scheduled-actions]
   :event [#'default-event-handler
           #'run-event-subs]})

(defn game
  ([]
   (game {}))
  ([subm & more]
   (apply add-subm {:settings default-settings}
          default-subm
          subm
          more)))