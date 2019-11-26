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

(deftest test-print-when-violations
  (testing "Should return the account after print"
    (is (= (print-when-violations {:account {}}) {:account {}}))
    (is (= (print-when-violations {:account {} :violations ()}) {:account {} :violations ()}))))

(deftest test-card-active
  (testing "Should return the account when card is active"
    (is (= (card-active {:account {:activeCard true}}) {:account {:activeCard true}}))))

(deftest test-card-inactive
  (testing "Should return violations when card is inactive"
    (is (= (card-active {:account {:activeCard false}}) {:account {:activeCard false} :violations ["card-not-active"]}))))

(deftest test-use-limit
  (testing "Should return the available limit after usage"
    (is (= (use-limit {:transaction {:amount 5}} {:account {:availableLimit 10}}) {:account {:availableLimit 5}}))))

(deftest test-sufficient-limit
  (testing "Should return the new availableLimit and the authorized key with list of tx"
    (is (= {:account {:availableLimit 5} :authorized [{:transaction {:amount 5}}]} (sufficient-limit {:transaction {:amount 5}} {:account {:availableLimit 10}})))))

(deftest test-no-sufficient-limit
  (testing "Shoudl return violations when has no sufficient limit"
    (is (= {:account {:availableLimit 10} :violations ["insufficient-limit"]} (sufficient-limit {:transaction {:amount 15}} {:account {:availableLimit 10}})))))
