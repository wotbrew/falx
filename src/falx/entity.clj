(ns falx.entity)

(defn cell
  [level point]
  {:point point
   :level level})

(defn slot
  [level point layer]
  {:point point
   :level level
   :layer layer})

(defn slice
  [level layer]
  {:level level
   :layer layer})

(defn put
  [entity cell]
  (let [{:keys [level point]} cell
        layer (:layer entity :misc)]
    (assoc entity
      :cell cell
      :slot (slot level point layer)
      :slice (slice level layer)
      :level level
      :point point)))
