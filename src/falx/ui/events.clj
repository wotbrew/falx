(ns falx.ui.events)

(defn change-screen
  [screen]
  {:event/type :event/change-screen
   :screen screen})

(defn update-state
  ([f]
   {:event/type :event/update-ui-state
    :f          f})
  ([f & args]
    (update-state #(apply f % args))))