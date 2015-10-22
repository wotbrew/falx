(ns falx.event)

(defonce handlers (atom {}))

(defn register-handler!
  [event-type name f]
  (swap! handlers assoc-in [event-type name] f))

(defn get-handler-map
  [event-type]
  (get @handlers event-type))

(defn publish!
  [event]
  (let [handlers (get-handler-map (:event/type event))]
    (run! #(% event) (vals handlers))
    nil))

(defn publish-all!
  [coll]
  (run! publish! coll)
  nil)