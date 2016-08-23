(ns falx.entity)

(defn pos
  ([map pt]
   {::map map
    ::point pt})
  ([map x y]
   (pos map [x y])))

(defn slice
  [map layer]
  {::map map
   ::layer layer})

(defn put
  ([e pos]
   (let [map (::map pos)
         pt (::point pos)
         layer (::layer e)]
     (assoc e
       ::pos pos
       ::map map
       ::point pt
       ::slice (slice map layer))))
  ([e map pt]
   (put e (pos map pt)))
  ([e map x y]
   (put e map [x y])))

(defn unput
  [e]
  (dissoc e ::pos ::map ::point ::slice))

(defn obstructs?
  [a b]
  (and (::solid? a)
       (::solid? b)))

(defn map-pos
  ([e pt]
   (pos (::map e) pt))
  ([e x y]
   (map-pos e [x y])))

(defn create
  [type & {:as kvs}]
  (merge
    {::type type}
    kvs))