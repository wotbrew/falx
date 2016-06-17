(ns falx.geom-test
  (:require [falx.geom :as g]
            [falx.geom.spec]
            [clojure.test :refer :all]
            [clojure.spec.test :as st]))

(defmacro is-spec?
  [v]
  `(is (-> (st/check-var ~v)
           :result
           true?)))

(deftest x-spec-passed?
  (is-spec? #'g/x))

(deftest y-spec-passed?
  (is-spec? #'g/y))

(deftest w-spec-passed?
  (is-spec? #'g/w))

(deftest h-spec-passed?
  (is-spec? #'g/h))

(deftest point-spec-passed?
  (is-spec? #'g/point))

(deftest size-spec-passed?
  (is-spec? #'g/size))

(deftest rect-spec-passed?
  (is-spec? #'g/rect))

(deftest add-spec-passed?
  (is-spec? #'g/add))

(deftest mult-spec-passed?
  (is-spec? #'g/mult))

(deftest sub-spec-passed?
  (is-spec? #'g/sub))