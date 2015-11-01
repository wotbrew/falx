(ns gdx.keyboard
  (:require [clojure.set :as set])
  (:import (com.badlogic.gdx Gdx Input Input$Keys)))

(defn ^Input get-input
  []
  (when-some [app Gdx/app]
    (.getInput app)))

(defn gdx-key-pressed?
  [gdx-key]
  (if-some [input (get-input)]
    (.isKeyPressed input gdx-key)
    false))


(def gdx-number-key->key
  (->> (for [n (range 0 10)
             ]
         [(+ Input$Keys/NUM_0 n) (keyword (str "num-" n))])
       (into {})))

(def gdx-char-key->key
  (->> (for [n (range Input$Keys/A (inc Input$Keys/Z))]
         [n (keyword (str (char (+ n 68))))])
       (into {})))

(def gdx-f-key->key
  (->> (for [n (range Input$Keys/F1 (inc Input$Keys/F12))]
         [n (keyword (str "f" (- n 243)))])
       (into {})))

(def gdx-key->key
  (merge
    gdx-number-key->key
    gdx-char-key->key
    gdx-f-key->key
    { Input$Keys/SHIFT_LEFT :left-shift
     Input$Keys/SHIFT_RIGHT :right-shift
     Input$Keys/CONTROL_LEFT :left-ctrl
     Input$Keys/CONTROL_RIGHT  :right-ctrl
     Input$Keys/SPACE  :space
     Input$Keys/BACKSPACE    :backspace }))

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

(defn get-next-keyboard
  [previous-keyboard]
  (let [now (get-keyboard)]
    (assoc now
      :hit (set/difference (:pressed previous-keyboard #{})
                           (:pressed now #{})))))