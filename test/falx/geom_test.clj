(ns falx.geom-test
  (:require [falx.geom :as g]
            [falx.geom.spec]
            [clojure.test :refer :all]
            [clojure.spec.test :as st]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.spec :as s]))

(deftest specs-pass?
  (let [r (st/run-tests 'falx.geom)]
    (= (:test r)
       (:pass r))))

(defspec point-on-point-is-identity
  (prop/for-all
    [x (s/gen ::g/point)]
    (= x (g/point x))))

(defspec point-on-rect-selects-xy
  (prop/for-all
    [x (s/gen ::g/rect)]
    (= (select-keys x [::g/x ::g/y]) (g/point x))))

(defspec point-on-size-returns-nil
  (prop/for-all
    [x (s/gen ::g/size)]
    (nil? (g/point x))))

(defspec point-on-ints-returns-xy
  (prop/for-all
    [x (s/gen ::g/x)
     y (s/gen ::g/y)]
    (= (g/point x y)
       {::g/x x ::g/y y})))

(defspec size-on-size-is-identity
  (prop/for-all
    [x (s/gen ::g/size)]
    (= x (g/size x))))

(defspec size-on-rect-selects-wh
  (prop/for-all
    [x (s/gen ::g/rect)]
    (= (select-keys x [::g/w ::g/h]) (g/size x))))

(defspec size-on-point-returns-nil
  (prop/for-all
    [x (s/gen ::g/point)]
    (nil? (g/size x))))

(defspec size-on-ints-returns-wh
  (prop/for-all
    [w (s/gen ::g/w)
     h (s/gen ::g/h)]
    (= (g/size w h)
       {::g/w w ::g/h h})))

(defspec rect-on-rect-is-identity
  (prop/for-all
    [x (s/gen ::g/rect)]
    (= x (g/rect x))))

(defspec rect-on-point-returns-nil
  (prop/for-all
    [x (s/gen ::g/point)]
    (nil? (g/rect x))))

(defspec rect-on-size-returns-nil
  (prop/for-all
    [x (s/gen ::g/size)]
    (nil? (g/rect x))))

(defspec add-will-add-vals-together
  (prop/for-all
    [g1 (s/gen ::g/geom)
     g2 (s/gen ::g/geom)]
    (= (merge-with +' g1 g2)
       (g/add g1 g2))))

(defspec mult-will-mult-vals-together
  (prop/for-all
    [g1 (s/gen ::g/geom)
     g2 (s/gen ::g/geom)]
    (= (merge-with *' g1 g2)
       (g/mult g1 g2))))

(defspec sub-will-sub-vals-from-one-another
  (prop/for-all
    [g1 (s/gen ::g/geom)
     g2 (s/gen ::g/geom)]
    (= (merge-with -' g1 g2)
       (g/sub g1 g2))))
