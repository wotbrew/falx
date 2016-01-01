(ns falx.thing.creature)

(defn creature?
  [thing]
  (= (:type thing) :creature))