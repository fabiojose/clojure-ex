(ns userin.core-test
  (:require [clojure.test :refer :all]
            [userin.core :refer :all]))

(deftest test-json-parse
  (testing "Should return a parsed json"
    (is (= {:name "clojure"} (json-parse "{\"name\" : \"clojure\"}"))))
  (testing "Should throw exception on invalid json"
    (is (thrown? Exception (json-parse "llç")))))

(deftest test-decide
  (testing "Should throw exception on unsupported json"
    (is (thrown? Exception (decide {:strange "strange json"})))))

(deftest test-is-a-text
  (testing "Should return false when text is not valid"
    (is (= (is-a-text "") false))
    (is (= (is-a-text nil) false))))

(deftest test-is-account
  (testing "Should return false when json does not contains :account"
    (is (= (is-account {:tx "nope"}) false))
    (is (= (is-account {:account "yep"})))))

(deftest test-limit-of-account
  (testing "Should return the availableLimit of account"
    (is (= (limit-of {:account {:availableLimit 100}}) 100))))

(deftest test-amount-of-tx
  (testing "Should return the amount of transaction"
    (is (= (amount-of {:transaction {:amount 200}}) 200))))

(deftest test-has-violations
  (testing "Should return true when has :violations"
    (is (= (has-violations {:account {} :violations ()}) true))))
