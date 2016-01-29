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

(def set-conj (fnil conj #{}))

(defn find-first
  [pred coll]
  (first (filter pred coll)))

#_(defn identity-memoize
  "Not thread safe, won't handle switching arity either"
  [f]
  (let [varr (object-array 32)
        rarr (object-array 32)]
    (fn
      ([x]
       (if (identical? (aget varr 0) x)
         (aget rarr 0)
         (let [r (f x)]
           (aset varr 0 x)
           (aset rarr 0 r)
           r)))
      ([x y]
       (if (and (identical? (aget varr 0) x)
                (identical? (aget varr 1) y))
         (aget rarr 1)
         (let [r (f x y)]
           (aset varr 0 x)
           (aset varr 1 y)
           (aset rarr 1 r))))
      ([x y z]
       (if (and (identical? (aget varr 0) x)
                (identical? (aget varr 1) y)
                (identical? (aget varr 2) z))
         (aget rarr 2)
         (let [r (f x y z)]
           (aset varr 0 x)
           (aset varr 1 y)
           (aset varr 2 z)
           (aset rarr 2 r)))))))