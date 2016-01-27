(ns falx.react)

(defn reaction
  [key f]
  {:key key
   :f f})

(defn register
  [rm reaction]
  (let [{:keys [key f]} reaction]
    (update rm key conj f)))

(defn react-map
  [reactions]
  (reduce register {} reactions))

(defn react
  [v rm event]
  (let [k (:type event)
        fs (get rm k)]
    (reduce #(%2 %1 event) v fs)))