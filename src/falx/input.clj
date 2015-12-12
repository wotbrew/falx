(ns falx.input)

(defn bind-key-pressed
  [id key]
  {:id id
   :type :key-pressed
   :pressed key})

(defn bind-key-hit
  [id key]
  {:id id
   :type :key-hit
   :hit key})

(defn bind-button-pressed
  [id button]
  {:id id
   :type :button-pressed
   :pressed button})

(defn bind-button-hit
  [id button]
  {:id id
   :type :button-hit
   :hit button})

(defn get-binding-key
  [binding]
  (case (:type binding)
    :key-pressed [:key-pressed (:pressed binding)]
    :key-hit [:key-hit (:hit binding)]
    :button-pressed [:button-pressed (:pressed binding)]
    :button-hit [:button-hit (:hit binding)]
    nil))

(defn modifiers-pressed?
  [input binding]
  (every? (-> input :keyboard :pressed (or #{})) (:modifiers binding)))

(defn get-commands
  [input binding-map]
  (let [{:keys [keyboard mouse]} input]
    (concat
      (for [pressed (:pressed keyboard)
            binding (get binding-map [:key-pressed pressed])
            :when (modifiers-pressed? input binding)]
        (:id binding))
      (for [hit (:hit keyboard)
            binding (get binding-map [:key-hit hit])
            :when (modifiers-pressed? input binding)]
        (:id binding))
      (for [pressed (:pressed mouse)
            binding (get binding-map [:button-pressed pressed])
            :when (modifiers-pressed? input binding)]
        (:id binding))
      (for [hit (:hit mouse)
            binding (get binding-map [:button-hit hit])
            :when (modifiers-pressed? input binding)]
        (:id binding)))))