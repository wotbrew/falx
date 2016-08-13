(ns falx.engine.input
  (:require [falx.engine.mouse :as mouse]
            [falx.engine.keyboard :as keyboard])
  (:refer-clojure :exclude [compile]))

(defn combine
  ([prev new]
   {::mouse (mouse/combine (::mouse prev) (::mouse new))
    ::keyboard (keyboard/combine (::keyboard prev) (::keyboard new))}))

(defn now
  ([]
   {::mouse (mouse/now)
    ::keyboard (keyboard/now)})
  ([prev]
   (combine prev (now))))

(defn either
  [& binds]
  [:either binds])

(defn combo
  [& binds]
  [:combo binds])

(defn hit
  [& binds]
  (apply combo (mapv (partial vector :hit) binds)))

(defn pressed
  [& binds]
  (apply combo (mapv (partial vector :pressed) binds)))

(defn checkfn
  [binding]
  (let [[k rest] binding]
    (case k
      :hit (cond
             (keyboard/key? rest) (fn [input] (keyboard/hit? (::keyboard input) rest))
             (mouse/button? rest) (fn [input] (mouse/hit? (::mouse input) rest))
             :else (constantly false))
      :pressed (cond
                 (keyboard/key? rest) (fn [input] (keyboard/pressed? (::keyboard input) rest))
                 (mouse/button? rest) (fn [input] (mouse/pressed? (::mouse input) rest))
                 :else (constantly false))
      :either (let [fs (mapv checkfn rest)]
                (apply some-fn fs))
      :combo (let [fs (mapv checkfn rest)]
               (apply every-pred fs))
      :compiled rest
      (throw (ex-info "No known input key" {:key k})))))

(defn compile
  [binding]
  (with-meta [:compiled (checkfn binding)] {:input binding}))

(defn check
  [input binding]
  ((checkfn binding) input))

(defn mod?
  [input]
  (keyboard/pressed? (::keyboard input) ::keyboard/key.shift-left))

(defn query
  [input bindmap]
  (for [[k bind] bindmap
        :when (check input bind)]
    k))