(ns falx.gdx.keyboard
  (:import (com.badlogic.gdx Gdx Input Input$Keys))
  (:refer-clojure :exclude [keys]))

(defn- ^Input input
  []
  (when-some [app Gdx/app]
    (.getInput app)))

(def keys
  {::key.o Input$Keys/O,
   ::key.a Input$Keys/A,
   ::key.b Input$Keys/B,
   ::key.n Input$Keys/N,
   ::key.g Input$Keys/G,
   ::key.d Input$Keys/D,
   ::key.h Input$Keys/H,
   ::key.v Input$Keys/V,
   ::key.p Input$Keys/P,
   ::key.l Input$Keys/L,
   ::key.q Input$Keys/Q,
   ::key.s Input$Keys/S,
   ::key.k Input$Keys/K,
   ::key.e Input$Keys/E,
   ::key.x Input$Keys/X,
   ::key.c Input$Keys/C,
   ::key.t Input$Keys/T,
   ::key.y Input$Keys/Y,
   ::key.f Input$Keys/F,
   ::key.m Input$Keys/M,
   ::key.w Input$Keys/W,
   ::key.i Input$Keys/I,
   ::key.z Input$Keys/Z,
   ::key.u Input$Keys/U,
   ::key.r Input$Keys/R,
   ::key.j Input$Keys/J

   ::key.esc Input$Keys/ESCAPE
   ::key.shift-left Input$Keys/SHIFT_LEFT
   ::key.shift-right Input$Keys/SHIFT_RIGHT})

(defn pressed?
  [key]
  (if-some [input (input)]
    (.isKeyPressed input (keys key))
    false))

(defn pressed
  []
  (into #{} (filter pressed?) (clojure.core/keys keys)))