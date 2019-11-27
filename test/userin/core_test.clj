(ns userin.core-test
  (:require [clojure.test :refer :all]
            [userin.core :refer :all]
            [java-time :as j]))

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

(deftest test-parse-tx
  (testing "Should parse the time field"
    (is (= {:transaction {:time (j/zoned-date-time "2019-02-13T10:00:00.000Z")}} (parse-tx {:transaction {:time "2019-02-13T10:00:00.000Z"}})))))

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
    (is (= {:account {:availableLimit 5} :authorized [{:transaction {:amount 5 :time (j/zoned-date-time "2019-02-13T10:00:00.000Z")}}]} (sufficient-limit {:transaction {:amount 5 :time "2019-02-13T10:00:00.000Z"}} {:account {:availableLimit 10}}))))
  (testing "Should return the same account when has violations"
    (is (= {:account {:availableLimit 10} :violations []} (sufficient-limit {:transaction {:amount 5}} {:account {:availableLimit 10} :violations []})))))

(deftest test-no-sufficient-limit
  (testing "Should return violations when has no sufficient limit"
    (is (= {:account {:availableLimit 10} :violations ["insufficient-limit"]} (sufficient-limit {:transaction {:amount 15}} {:account {:availableLimit 10}})))))

(deftest test-already-initialized
  (testing "Should return violations when account already exists"
    (is (= {:account {:activeCard true} :violations ["account-already-initialized"]} (already-initialized {:account {:activeCard true}} {:account {:activeCard true}}))))
  (testing "Should return the json when account is nil"
    (is (= {:account {:activeCard true}} (already-initialized {:account {:activeCard true}} nil)))))
;
; (deftest test-get-item
;   (testing "Should return the right item"
;     (is (= 6 (get-it-reverse [4 5 6 7] 2)))))
