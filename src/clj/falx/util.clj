(ns falx.util)

(defn disjoc
  [m k v]
  (let [s (get m k)]
    (if-some [new (not-empty (disj s v))]
      (assoc m k new)
      (dissoc m k))))

(defn disjoc-in
  [m [k & ks] v]
  (if (seq ks)
    (if-some [m2 (not-empty (disjoc-in (get m k) ks v))]
      (assoc m k m2)
      (dissoc m k))
    (disjoc m k v)))

(defn dissoc-in
  [m [k & ks]]
  (if (seq ks)
    (if-some [m2 (not-empty (dissoc-in (get m k) ks))]
      (assoc m k m2)
      (dissoc m k))
    (dissoc m k)))

(defn pipe
  ([f]
    f)
  ([f g]
    (fn
      ([a] (g (f a)))
      ([a b] (g (f a b)))
      ([a b c] (g (f a b c)))
      ([a b c d] (g (f a b c d)))
      ([a b c d & more] (g (apply  f a b c d more)))))
  ([f g & fs]
    (reduce (fn [g f]
              (pipe f g))
            (pipe f g)
            fs)))