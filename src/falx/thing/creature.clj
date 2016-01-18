(ns falx.thing.creature)

(defn creature?
  [thing]
  (= (:type thing) :creature))

(defn player?
  [thing]
  (= (:faction thing) :faction/player))

(def base-template
  {:type :creature
   :layer :creature
   :solid? true
   :faction :faction/player})

(def human
  (merge base-template
         {:race :human
          :gender :male}))

(def goblin-template
  (merge base-template
         {:race :goblin
          :gender :male
          :faction :faction/goblin}))