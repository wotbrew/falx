(ns falx.ui-test
  (:require [clojure.test :refer :all]
            [falx.ui :as ui]
            [falx.rect :as rect]))

(deftest creating-a-label
  (let [label (ui/label [0 0 96 32] "foobar")]
    (is (= [0 0 96 32] (:rect label)))
    (is (= "foobar" (:text label)))))
