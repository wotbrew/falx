(ns falx.input
  (:require [falx.gdx :as gdx]
            [falx.rect :as rect]
            [clojure.set :as set]))

(defrecord InputState
  [mouse
   keys-pressed
   keys-hit
   buttons-pressed
   buttons-hit])

(defn input-state
  ([]
    (map->InputState
      {:mouse (gdx/mouse)
       :keys-pressed (gdx/keys-pressed)
       :keys-hit #{}
       :buttons-pressed (gdx/buttons-pressed)
       :buttons-hit #{}}))
  ([previous-state]
    (input-state previous-state (input-state)))
  ([previous-state state]
    (assoc state
      :keys-hit (set/difference (:keys-pressed previous-state)
                                (:keys-pressed state))
      :buttons-hit (set/difference (:buttons-hit previous-state)
                                   (:buttons-hit state)))))

(defprotocol IBinding
  (-hit? [this is]))

(defrecord KeyHitBinding [key]
  IBinding
  (-hit? [this is]
    (contains? (:keys-hit is) key)))

(defn key-hit
  [key]
  (->KeyHitBinding key))

(defrecord KeyPressedBinding [key]
  IBinding
  (-hit? [this is]
    (contains? (:keys-pressed is) key)))

(defn key-pressed
  [key]
  (->KeyPressedBinding key))

(defrecord ButtonHitBinding [btn]
  IBinding
  (-hit? [this is]
    (contains? (:buttons-hit is) btn)))

(defn button-hit
  [btn]
  (->ButtonHitBinding btn))

(defrecord ButtonPressedBinding [btn]
  IBinding
  (-hit? [this is]
    (contains? (:buttons-pressed is) btn)))

(defn button-pressed
  [btn]
  (->ButtonPressedBinding btn))

(defrecord MouseIn [rect]
  IBinding
  (-hit? [this is]
    (rect/contains-pt? rect (:mouse is))))

(defn mouse-in
  [rect]
  (->MouseIn rect))

(defn hit?
  [is binding]
  (cond
    (vector? binding) (every? (partial hit? is) binding)
    (set? binding) (some (partial hit? is) binding)
    :else (-hit? binding is)))
