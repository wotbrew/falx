(ns falx.event)

(defonce handlers (atom {}))

(defn handler
  [type key f]
  {:type type
   :key key
   :fn f})

(defn register!
  [handler]
  (swap! handlers assoc-in [(:type handler) (:key handler)] handler))

(defn unregister!
  [handler]
  (swap! handlers update (:type handler) dissoc :key))

(defmacro defhandler
  [type key f]
  `(register! (handler ~type ~key ~f)))

(defn publish!
  [event]
  (let [handlers (-> @handlers (get (:type event)))]
    (run! #((:fn %) event) (vals handlers))))
