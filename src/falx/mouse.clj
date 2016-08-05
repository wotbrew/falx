(ns falx.mouse
  (:require [falx.state :as state]
            [falx.gdx :as gdx]
            [falx.point :as pt]
            [falx.gdx.mouse :as gdx-mouse]
            [clojure.set :as set]))

(def buttons
  {::button.left ::gdx-mouse/button.left
   ::button.right ::gdx-mouse/button.right})

(defn button?
  [x]
  (contains? buttons x))

(def rbuttons
  (into {} (map (juxt val key)) buttons))

(state/defsignal
  ::mouse
  (gdx/signal
    (let [ms @gdx-mouse/point]
      {::point @gdx-mouse/point
       ::pressed (set (keep rbuttons (gdx-mouse/pressed)))}))
  :merge
  (fn [old new]
    (let [hit (set/difference
                (::pressed old #{})
                (::pressed new #{}))]
      (assoc new ::hit hit))))

(defn in?
  [mouse rect]
  (pt/in? (::point mouse [0 0]) rect))

(defn hit?
  ([mouse button]
   (contains? (::hit mouse) button)))

(defn pressed?
  ([mouse button]
   (contains? (::pressed mouse) button)))

(defn left-click?
  ([mouse]
   (hit? mouse ::button.left))
  ([mouse rect]
   (and (left-click? mouse) (in? mouse rect))))

(defn right-click?
  ([mouse]
   (hit? mouse ::button.right))
  ([mouse rect]
   (and (right-click? mouse) (in? mouse rect))))