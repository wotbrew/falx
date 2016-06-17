(ns falx.rect-test
  (:require [falx.rect :as rect]
            [falx.rect.spec]
            [clojure.test :refer :all]
            [clojure.spec.test :as st]))

(defmacro is-spec?
  [v]
  `(is (-> (st/check-var ~v)
           :result
           true?)))

(deftest add-spec-passed?
  (is-spec? #'rect/add))

(deftest mult-spec-passed?
  (is-spec? #'rect/mult))

(deftest scale-spec-passed?
  (is-spec? #'rect/scale))

(deftest expand-spec-passed?
  (is-spec? #'rect/expand))

(deftest shift-spec-passed?
  (is-spec? #'rect/shift))

(deftest lshift-spec-passed?
  (is-spec? #'rect/lshift))

(deftest sub-spec-passed?
  (is-spec? #'rect/sub))