(ns falx.user
  (:require [falx.config :as config]
            [falx.engine.input :as input]
            [falx.engine.keyboard :as keyboard])
  (:refer-clojure :exclude [binding]))

(def default-bindings
  (cond->>
    {::bind.back (input/hit ::keyboard/key.esc)

     ::bind.cam-up (input/pressed ::keyboard/key.w)
     ::bind.cam-left (input/pressed ::keyboard/key.a)
     ::bind.cam-right (input/pressed ::keyboard/key.d)
     ::bind.cam-down (input/pressed ::keyboard/key.s)

     ::bind.mod (input/either
                  (input/pressed ::keyboard/key.shift-left)
                  (input/pressed ::keyboard/key.shift-right))}
    config/optimise? (reduce-kv #(assoc %1 %2 (input/compile %3)) {})))

(defn get-binding
  [user key]
  (or (-> user ::bindings (get key))
      (default-bindings key)))

(defn binding
  [key]
  (fn [user input]
    (input/check input (get-binding user key))))

(def default-settings
  {::setting.resolution [800 600]
   ::setting.cell-size [32 32]
   ::setting.cam-speed 1.0})

(defn get-setting
  [user key]
  (or (-> user ::settings (get key))
      (default-settings key)))

(defn set-setting
  [user key val]
  (assoc-in user [::settings key] val))

(defn setting
  [key]
  (fn
    ([]
     (default-settings key))
    ([user]
     (get-setting user key))))