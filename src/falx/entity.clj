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
  ([level pt]
   {::pos.level level
    ::pos.point pt}))

(defn put
  ([e pos]
   (assoc e
     ::pos pos
     ::slice (slice (::pos.level pos) (::layer e))
     ::level (::pos.level pos)
     ::point (::pos.point pos)))
  ([e level pt]
   (put e (pos level pt)))
  ([e level x y]
   (put e level [x y])))

(defn unput
  [e]
  (dissoc e ::pos ::slice ::level ::point))

(defn step
  ([e pt]
   (put e (::level e :falx.level/limbo) pt))
  ([e x y]
   (step e [x y])))