(ns falx.point-test
  (:require [falx.point :as point]
            [falx.point.spec]
            [clojure.test :refer :all]
            [clojure.spec.test :as st]))

(defmacro is-spec?
  [v]
  `(is (-> (st/check-var ~v)
           :result
           true?)))

(deftest add-spec-passed?
  (is-spec? #'point/add))

(deftest mult-spec-passed?
  (is-spec? #'point/mult))

(deftest scale-spec-passed?
  (is-spec? #'point/scale))

(deftest shift-spec-passed?
  (is-spec? #'point/shift))

(deftest lshift-spec-passed?
  (is-spec? #'point/lshift))

(deftest sub-spec-passed?
  (is-spec? #'point/sub))

(deftest line-right-spec-passed?
  (is-spec? #'point/line-right))

(deftest line-left-spec-passed?
  (is-spec? #'point/line-left))

(deftest line-up-spec-passed?
  (is-spec? #'point/line-up))

(deftest line-down-spec-passed?
  (is-spec? #'point/line-down))