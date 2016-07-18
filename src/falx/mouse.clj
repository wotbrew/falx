(ns falx.mouse
  (:require [clj-gdx :as gdx]
            [falx.geom :as g]
            [falx.button :as button])
  (:refer-clojure :exclude [next]))

(def initial-buttons
  (->> (for [button (set (vals button/gdx-button->button))]
         [button (button/initial-state button)])
       (into {})))

(def initial
  {::point (g/point)
   ::buttons initial-buttons})

(defn next
  ([]
    initial)
  ([mouse gdx-mouse delta-time]
   (let [{:keys [point hit pressed]} gdx-mouse]
     {::point (g/point-tuple->geom point)
      ::buttons (reduce-kv (fn [m k v]
                             (let [gdx-button (button/button->gdx k)
                                   pressed? (contains? pressed gdx-button)
                                   hit? (contains? hit gdx-button)]
                               (assoc m k (button/next-state v {:pressed? pressed?
                                                                :hit? hit?
                                                                :delta-time delta-time}))))
                           {}
                           (::buttons mouse))})))

(defn button-state
  [mouse button]
  (-> mouse :buttons (get button)))

(defn hit?
  [mouse button]
  (::button/hit? (button-state mouse button)))

(defn pressed?
  [mouse button]
  (::button/pressed? (button-state mouse button)))