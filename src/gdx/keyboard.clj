(ns gdx.keyboard
  (:require [clojure.set :as set]
            [clojure.string :as str])
  (:import (com.badlogic.gdx Gdx Input Input$Keys Input$TextInputListener)))

(defn ^Input get-input
  []
  (when-some [app Gdx/app]
    (.getInput app)))

(defn gdx-key-pressed?
  [gdx-key]
  (if-some [input (get-input)]
    (.isKeyPressed input gdx-key)
    false))

(def prefer-keys
  (set/union
    (set (map str "qwertyuiopasdfghjklzxcvbnm"))))

(def gdx-key->key
  (->> (for [field (.getFields Input$Keys)
             :let [n (.getName field)
                   v (.getInt field nil)]]
         [v (keyword (-> n
                         str/lower-case
                         (str/replace #"_" "-")))])
       (sort-by #(if (prefer-keys (name (second %))) 1 0))
       (into {})))

(def key->gdx-key
  (reduce-kv #(assoc %1 %3 %2) {} gdx-key->key))

(defn key-pressed?
  [key]
  (gdx-key-pressed? (key->gdx-key key)))

(def all-keys
  (set (keys key->gdx-key)))

(defn get-keyboard
  []
  {:pressed (into #{} (filter key-pressed? all-keys))})

(def default
  {:pressed #{}
   :hit #{}})

(defn get-next-keyboard
  [previous-keyboard]
  (let [now (get-keyboard)]
    (assoc now
      :hit (set/difference (:pressed previous-keyboard #{})
                           (:pressed now #{})))))

(defn get-string
  ([title]
    (get-string title ""))
  ([title hint]
    (get-string title hint ""))
  ([title hint s]
   (let [p (promise)
         listener (reify Input$TextInputListener
                    (input [this s]
                      (deliver p s))
                    (canceled [this]
                      (deliver p nil)))
         input (get-input)]
     (if input
       (do
         (.getTextInput input listener (str title) (str s) (str hint))
         p)
       (delay nil)))))