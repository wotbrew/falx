(ns falx.input
  (:require [falx.mouse :as mouse]
            [falx.keyboard :as keyboard])
  (:refer-clojure :exclude [compile]))

(defn either
  [& inputs]
  [:either inputs])

(defn combo
  [& inputs]
  [:combo inputs])

(defn hit
  [& inputs]
  (apply combo (mapv (partial vector :hit) inputs)))

(defn pressed
  [& inputs]
  (apply combo (mapv (partial vector :pressed) inputs)))

(defn checkfn
  [input]
  (let [[k rest] input]
    (case k
      :hit (cond
             (keyboard/key? rest) (fn [gs] (keyboard/hit? (::keyboard/keyboard gs) rest))
             (mouse/button? rest) (fn [gs] (mouse/hit? (::mouse/mouse gs) rest))
             :else (constantly false))
      :pressed (cond
                 (keyboard/key? rest) (fn [gs] (keyboard/pressed? (::keyboard/keyboard gs) rest))
                 (mouse/button? rest) (fn [gs] (mouse/pressed? (::mouse/mouse gs) rest))
                 :else (constantly false))
      :either (let [fs (mapv checkfn rest)]
                (apply some-fn fs))
      :combo (let [fs (mapv checkfn rest)]
               (apply every-pred fs))
      :compiled rest
      (throw (ex-info "No known input key" {:key k})))))

(defn compile
  [input]
  (with-meta [:compiled (checkfn input)] {:input input}))

(defn check
  [gs input]
  ((checkfn input) gs))

(defn mod?
  [gs]
  (keyboard/pressed? (::keyboard/keyboard gs) ::keyboard/key.shift-left))