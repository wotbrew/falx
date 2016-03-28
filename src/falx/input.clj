(ns falx.input)

(defrecord Input [keyboard mouse])

(defn input
  [keyboard mouse]
  (->Input keyboard mouse))