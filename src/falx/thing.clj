(ns falx.thing)

(defn split-events
  [thing]
  {:thing (dissoc thing :events)
   :events (:events thing)})

(defn publish-event
  [thing event]
  (update thing :events (fnil conj []) event))

(defn put
  [thing cell]
  (if (= cell (:cell thing))
    thing
    (-> (assoc thing
          :cell cell
          :slice {:layer (:layer thing :unknown)
                  :level (:level cell)}
          :point (:point cell)
          :level (:level cell))
        (publish-event
          {:type  :event.thing/put
           :thing thing
           :cell  cell}))))

(defn unput
  [thing]
  (-> (dissoc thing :cell :slice :point :level)
      (publish-event
        {:type :event.thing/unput
         :thing thing})))