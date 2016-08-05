(ns falx.keyboard
  (:require [falx.gdx.keyboard :as gdx-keyboard]
            [falx.state :as state]
            [falx.gdx :as gdx]
            [clojure.set :as set])
  (:refer-clojure :exclude [keys]))

(def keys
  {::key.b :gdx-keyboard/key.b,
   ::key.p :gdx-keyboard/key.p,
   ::key.m :gdx-keyboard/key.m,
   ::key.k :gdx-keyboard/key.k,
   ::key.r :gdx-keyboard/key.r,
   ::key.v :gdx-keyboard/key.v,
   ::key.i :gdx-keyboard/key.i,
   ::key.u :gdx-keyboard/key.u,
   ::key.z :gdx-keyboard/key.z,
   ::key.w :gdx-keyboard/key.w,
   ::key.q :gdx-keyboard/key.q,
   ::key.t :gdx-keyboard/key.t,
   ::key.e :gdx-keyboard/key.e,
   ::key.l :gdx-keyboard/key.l,
   ::key.j :gdx-keyboard/key.j,
   ::key.o :gdx-keyboard/key.o,
   ::key.x :gdx-keyboard/key.x,
   ::key.y :gdx-keyboard/key.y,
   ::key.f :gdx-keyboard/key.f,
   ::key.c :gdx-keyboard/key.c,
   ::key.a :gdx-keyboard/key.a,
   ::key.g :gdx-keyboard/key.g,
   ::key.d :gdx-keyboard/key.d,
   ::key.n :gdx-keyboard/key.n,
   ::key.s :gdx-keyboard/key.s,
   ::key.h :gdx-keyboard/key.h

   ::key.esc ::gdx-keyboard/key.esc})

(def rkeys
  (into {} (map (juxt val key)) keys))

(state/defsignal
  ::keyboard
  (gdx/signal
    {::pressed (set (keep rkeys (gdx-keyboard/pressed)))})
  :merge
  (fn [old new]
    (let [hit (set/difference
                (::pressed old #{})
                (::pressed new #{}))]
      (assoc new ::hit hit))))

(defn hit?
  ([keyboard key]
   (contains? (::hit keyboard) key)))

(defn pressed?
  ([keyboard key]
   (contains? (::pressed keyboard) key)))