(ns userin.funs-test
  (:require [clojure.test :refer :all]
            [userin.funs :refer :all]
            [java-time :as j]))

(deftest test-json-parse
  (testing "Should return a parsed json"
    (is (= {:name "clojure"} (json-parse "{\"name\" : \"clojure\"}"))))
  (testing "Should throw exception on invalid json"
    (is (thrown? Exception (json-parse "llç")))))

(deftest test-text?
  (testing "Should return false when text is not valid"
    (is (= (text? "") false))
    (is (= (text? nil) false))))

(deftest test-get-item-reverse
  (testing "Should return the right item"
    (is (= 102 (get-it-reverse [100 101 102 104] 1))))
  (testing "Should return nil when index does not exists"
    (is (empty? (get-it-reverse [4 5 6 7] 5))))
  (testing "Should return nil when list is empty"
    (is (empty? (get-it-reverse [] 3)))))

(deftest test-as-minutes
  (testing "Should return the value in minutes"
    (is (= 2 (as-minutes (j/duration 120 :seconds) 0))))
  (testing "Should return alternative value when duration is nil"
    (is (= 0 (as-minutes nil 0)))))
