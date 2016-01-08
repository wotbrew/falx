(ns falx.thing.creature)

(defn creature?
  [thing]
  (= (:type thing) :creature))

(def template
  {:type :creature
   :layer :creature
   :solid? true})