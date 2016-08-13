(ns falx.engine.mouse
  (:require [falx.engine.point :as pt]
            [falx.gdx.mouse :as gdx-mouse]
            [clojure.set :as set]))

(def ^:private buttons
  {::button.left ::gdx-mouse/button.left
   ::button.right ::gdx-mouse/button.right})

(def ^:private rbuttons
  (into {} (map (juxt val key)) buttons))

(defn button?
  "Is the value a button?"
  [x]
  (contains? buttons x))

(defn combine
  "Combines 2 successive mouse states over time, in order
  to derive a new state"
  [prev now]
  (let [hit (set/difference (::pressed prev #{}) (::pressed now))
        delta (- (::time now) (::time prev 0))]
    (assoc now ::hit hit
               ::delta delta)))

(defn now
  "Returns the current mouse, if a previous state is supplied - more information can be derived."
  ([]
   {::time (System/currentTimeMillis)
    ::point @gdx-mouse/point
    ::pressed (set (keep rbuttons (gdx-mouse/pressed)))})
  ([prev]
   (combine prev (now))))

(defn in?
  "Is the mouse in the rectangle?"
  ([mouse rect]
   (pt/in? (::point mouse) rect))
  ([mouse x y w h]
   (pt/in? (::point mouse) x y w h)))

(defn hit?
  "Is the button hit? i.e has it just been released?"
  ([mouse button]
   (contains? (::hit mouse) button)))

(defn pressed?
  "Is the button pressed down?"
  ([mouse button]
   (contains? (::pressed mouse) button)))

(defn hit-in?
  "Convenience function that asks both hit? and in?"
  ([mouse button rect]
    (and (hit? mouse button) (in? mouse rect))))

(defn pressed-in?
  "Convenience function that asks both pressed? and in?"
  ([mouse button rect]
    (and (pressed? mouse button) (in? mouse rect))))