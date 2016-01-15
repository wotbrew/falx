(ns falx.thing.creature)

(defn creature?
  [thing]
  (= (:type thing) :creature))

(def base-template
  {:type :creature
   :layer :creature
   :solid? true})

(def human
  (merge base-template
         {:race :human
          :gender :male}))

(def goblin-template
  (merge base-template
         {:race :goblin
          :gender :male}))