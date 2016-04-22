(ns falx.schedule)

(defn schedule
  []
  {:times-of {}
   :at-ms (sorted-map)})

(defn add-action
  [schedule ms action]
  (-> (update-in schedule [:times-of action] (fnil conj #{}) ms)
      (update-in [:at-ms ms] (fnil conj []) action)))

(defn get-until
  [schedule ms]
  (->> (subseq (:at-ms schedule) <= ms)
       (mapcat val)))

(defn remove-until
  [schedule ms]
  (let [kvs (subseq (:at-ms schedule) <= ms)]
    (update schedule :at-ms #(transduce (map key) dissoc % kvs))))

(defn get-action-schedule
  [schedule action]
  (-> schedule :times-of action))

(defn scheduled?
  [schedule action]
  (some? (seq (get-action-schedule schedule action))))

(defn remove-action
  ([schedule action]
   (->> (get-action-schedule schedule action)
        (reduce #(remove-action %1 action %2) schedule)))
  ([schedule action ms]
   (-> schedule
       (update :times-of dissoc action)
       (update :at-ms (fn [m]
                        (if-let [v (->> (get m ms)
                                        (remove action)
                                        vec)]
                          (assoc m ms v)
                          (dissoc m ms)))))))

