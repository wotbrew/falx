(ns falx.thing)

(defn party?
  [thing]
  (= (:type thing) :party))

(defn solid?
  [thing]
  (or (:solid? thing)
      (party? thing)))