(ns falx.action)

(defonce reactions (atom {}))

(defn reaction
  [type key f]
  {:type type
   :key key
   :fn f})

(defn register!
  [reaction]
  (swap! reactions assoc-in [(:type reaction) (:key reaction)] reaction))

(defn unregister!
  [reaction]
  (swap! reactions update (:type reaction) dissoc :key))

(defmacro defreaction
  [type key f]
  `(register! (reaction ~type ~key ~f)))

(defn react
  ([m action]
    (react m action @reactions))
  ([m action reactions]
   (let [actions (-> reactions (get (:type action)))]
     (reduce (fn [m reaction]
               ((:fn reaction) m action)) m (vals actions)))))