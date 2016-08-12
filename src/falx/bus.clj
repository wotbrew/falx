(ns falx.bus)

(defn- dissoc-in
  [m [k & ks]]
  (if (seq ks)
    (if-some [m2 (not-empty (dissoc-in (get m k) ks))]
      (assoc m k m2)
      (dissoc m k))
    (dissoc m k)))

(def subscribers
  (atom {}))

(defn sub
  [type f]
  {::sub.message.type type
   ::sub.fn f})

(defn unsub
  [key]
  (swap!
    subscribers
    (fn [m]
      (let [sub (get-in m [::by-key key])]
        (-> (dissoc-in m [::by-key key])
            (update type #(into [] (remove #{sub}) %)))))))

(defn defsub
  [key type f]
  (unsub key)
  (let [sub (sub type f)]
    (swap!
      subscribers
      (fn [m]
        (-> m
            (assoc-in [::by-key key] sub)
            (update type (fnil conj []) sub))))
    nil))

(defn fire
  [gs sub]
  (let [f (::sub.fn sub identity)]
    (f gs)))

(defn pub
  ([gs msg]
    (let [subs (get @subscribers (::message.type msg))]
      (reduce fire gs subs)))
  ([gs msg & more]
    (reduce pub (pub gs msg) more)))