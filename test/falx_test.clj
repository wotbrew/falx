(ns falx-test
  (:require [clojure.test :refer :all]
            [falx :refer :all]
            [clj-gdx :as gdx])
  (:import (com.badlogic.gdx Gdx)))

(use-fixtures :once (fn [f] (-main) (f)))

(deftest can-start-the-game
  (when-not Gdx/app
    (loop [i 0]
      (when (and (nil? Gdx/app) (<= i 1000))
        (Thread/sleep 100)
        (recur (inc i)))))
  (is (some? Gdx/app)))

(deftest mouse-should-get-updated-within-a-second
  (let [p (promise)]
    (add-watch gdx/mouse-state ::test (fn [_ _ _ _] (deliver p true)))
    (is (deref p 1000 false))))

(deftest keyboard-should-get-updated-within-a-second
  (let [p (promise)]
    (add-watch gdx/keyboard-state ::test (fn [_ _ _ _] (deliver p true)))
    (is (deref p 1000 false))))