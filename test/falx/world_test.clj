(ns falx.world-test
  (:require [falx.world :refer :all]
            [clojure.test :refer :all]))

(deftest publishing-event-adds-it-to-event-coll
  (is (= (:events (publish-event {} {:type :something}))
         [{:type :something}])))

(deftest split-events-removes-events-from-world
  (is  (-> (publish-event {} {:type :something})
           split-events
           :world
           :events
           nil?)))

(deftest split-events-puts-events-under-key
  (is (-> (publish-event {} {:type :something})
          split-events
          :events
          (= [{:type :something}]))))

(deftest can-get-a-thing-by-id
  (is (= {:id "fred"}
         (get-thing (add-thing {} {:id "fred"})
                    "fred"))))

(deftest cannot-get-a-thing-that-doesnt-exist
  (is (nil? (get-thing {} "fred"))))

(deftest can-readd-existing-thing
  (is (-> (add-thing {} {:id "fred"})
          (add-thing {:id "fred"
                      :name "foobar"})
          (get-thing "fred")
          (= {:id   "fred"
              :name "foobar"}))))

(deftest can-update-thing
  (is (-> (add-thing {} {:id "fred"})
          (update-thing
            "fred" assoc :foo :bar)
          (get-thing "fred")
          (= {:id "fred"
              :foo :bar}))))

