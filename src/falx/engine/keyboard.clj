(ns falx.engine.keyboard
  (:require [falx.gdx.keyboard :as gdx-keyboard]
            [clojure.set :as set])
  (:refer-clojure :exclude [keys]))

(def keys
  {::key.b ::gdx-keyboard/key.b,
   ::key.p ::gdx-keyboard/key.p,
   ::key.m ::gdx-keyboard/key.m,
   ::key.k ::gdx-keyboard/key.k,
   ::key.r ::gdx-keyboard/key.r,
   ::key.v ::gdx-keyboard/key.v,
   ::key.i ::gdx-keyboard/key.i,
   ::key.u ::gdx-keyboard/key.u,
   ::key.z ::gdx-keyboard/key.z,
   ::key.w ::gdx-keyboard/key.w,
   ::key.q ::gdx-keyboard/key.q,
   ::key.t ::gdx-keyboard/key.t,
   ::key.e ::gdx-keyboard/key.e,
   ::key.l ::gdx-keyboard/key.l,
   ::key.j ::gdx-keyboard/key.j,
   ::key.o ::gdx-keyboard/key.o,
   ::key.x ::gdx-keyboard/key.x,
   ::key.y ::gdx-keyboard/key.y,
   ::key.f ::gdx-keyboard/key.f,
   ::key.c ::gdx-keyboard/key.c,
   ::key.a ::gdx-keyboard/key.a,
   ::key.g ::gdx-keyboard/key.g,
   ::key.d ::gdx-keyboard/key.d,
   ::key.n ::gdx-keyboard/key.n,
   ::key.s ::gdx-keyboard/key.s,
   ::key.h ::gdx-keyboard/key.h

   ::key.esc ::gdx-keyboard/key.esc
   ::key.shift-left ::gdx-keyboard/key.shift-left
   ::key.shift-right ::gdx-keyboard/key.shift-right})

(def rkeys
  (into {} (map (juxt val key)) keys))

(defn key?
  [x]
  (contains? keys x))

(defn combine
  [prev new]
  (let [hit (set/difference (::pressed prev #{}) (::pressed new))]
    (assoc new ::hit hit
               ::delta (- (::time new) (::time prev 0)))))

(defn now
  ([]
    {::time (System/currentTimeMillis)
     ::pressed (set (keep rkeys (gdx-keyboard/pressed)))})
  ([prev]
    (combine prev (now))))

(defn hit?
  ([keyboard key]
   (contains? (::hit keyboard) key)))

(defn pressed?
  ([keyboard key]
   (contains? (::pressed keyboard) key)))