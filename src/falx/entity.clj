(ns falx.entity
  (:require [falx.draw :as d]))

(defmulti draw!
  (fn [entity gs x y w h]
    (::type entity)))

(defmethod draw! :default
  [_ _ x y w h]
  (d/draw! "?" x y w h))

(defmulti click
  (fn [gs e]
    (::type e)))

(defmethod click :default
  [gs e]
  gs)

(defmulti alt-click
  (fn [gs e]
    (::type e)))

(defmethod alt-click :default
  [gs e]
  gs)

(defn slice
  [level layer]
  {::slice.level level
   ::slice.layer layer})

(defn pos
  ([slice pt]
    (pos (::slice.level slice) (::slice.layer slice) pt))
  ([level layer pt]
   {::pos.level level
    ::pos.layer layer
    ::pos.point pt}))

(defn put
  ([e pos]
   (assoc e
     ::pos pos
     ::slice (slice (::pos.level pos) (::pos.layer pos))
     ::level (::pos.level pos)
     ::layer (::pos.layer pos)
     ::point (::pos.point pos)))
  ([e level pt]
   (put e {::pos.level level
           ::pos.layer (get e ::layer)
           ::pos.point pt}))
  ([e level layer pt]
   (put e {::pos.level level
           ::pos.layer layer
           ::pos.point pt})))

(defn unput
  [e]
  (dissoc e ::pos ::slice ::map ::point))
