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
    (is (= {:account {:availableLimit 5} :authorized [{:transaction {:amount 5 :time (j/zoned-date-time "2019-02-13T10:00:00.000Z")}}]} (sufficient-limit {:transaction {:amount 5 :time (j/zoned-date-time "2019-02-13T10:00:00.000Z")}} {:account {:availableLimit 10}}))))
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

(deftest test-get-item-reverse
  (testing "Should return the right item"
    (is (= 102 (get-it-reverse [100 101 102 104] 1))))
  (testing "Should return nil when index does not exists"
    (is (empty? (get-it-reverse [4 5 6 7] 5))))
  (testing "Should return nil when list is empty"
    (is (empty? (get-it-reverse [] 3)))))

(deftest test-time-diff-tx
  (testing "Should return the right amount of time"
    (is (= 2 (j/as (time-diff-tx {:transaction {:time (j/zoned-date-time "2019-11-28T10:00:00.000Z")}} {:transaction {:time (j/zoned-date-time "2019-11-28T10:02:00.000Z")}}) :minutes) )))
  (testing "Should return nil when arg0 is nil"
    (is (empty? (time-diff-tx nil {:transaction {:time (j/zoned-date-time "2019-11-28T10:02:00.000Z")}}))))
  (testing "Should return nil when arg1 is nil"
    (is (empty? (time-diff-tx {:transaction {:time (j/zoned-date-time "2019-11-28T10:02:00.000Z")}} nil)))))

(deftest test-as-minutes
  (testing "Should return the value in minutes"
    (is (= 2 (as-minutes (j/duration 120 :seconds) 0))))
  (testing "Should return alternative value when duration is nil"
    (is (= 0 (as-minutes nil 0)))))

(deftest test-high-frequency
  (testing "Should return the account itself when frequency is ok"
    (is (= {:account {:availableLimit 10}}
           (high-frequency
            {:transaction {:time (j/zoned-date-time "2019-11-28T10:00:00.000Z")}}
            [{:transaction {:time (j/zoned-date-time "2019-11-28T09:56:00.000Z")}}
             {:transaction {:time (j/zoned-date-time "2019-11-28T09:58:00.000Z")}}]
            3
            2
            {:account {:availableLimit 10}}))))
  (testing "Should return the account itself when there is no authorized yet"
    (is (= {:account {:availableLimit 10}}
           (high-frequency
            {:transaction {:time (j/zoned-date-time "2019-11-28T10:00:00.000Z")}}
            []
            3
            2
            {:account {:availableLimit 10}}))))
  (testing "Should return the account itself when authorized is nil"
    (is (= {:account {:availableLimit 10}}
           (high-frequency
            {:transaction {:time (j/zoned-date-time "2019-11-28T10:00:00.000Z")}}
            nil
            3
            2
            {:account {:availableLimit 10}}))))
  (testing "Should return violation when violate the constraint"
    (is (= {:account {:availableLimit 10} :violations ["high-frequency-small-interval"]}
           (high-frequency
            {:transaction {:time (j/zoned-date-time "2019-11-28T10:00:00.000Z")}}
            [{:transaction {:time (j/zoned-date-time "2019-11-28T09:58:00.000Z")}}
             {:transaction {:time (j/zoned-date-time "2019-11-28T09:59:00.000Z")}}]
            3
            2
            {:account {:availableLimit 10}}))))
  (testing "Should return the account itseld when there is no enogh authorized"
    (is (= {:account {:availableLimit 10}}
           (high-frequency
            {:transaction {:time (j/zoned-date-time "2019-11-28T10:00:00.000Z")}}
            [{:transaction {:time (j/zoned-date-time "2019-11-28T09:58:00.000Z")}}
             {:transaction {:time (j/zoned-date-time "2019-11-28T09:59:00.000Z")}}]
            4
            2
            {:account {:availableLimit 10}})))))

(deftest test-equals-tx
  (testing "Should return true when tx are equals"
   (is (= true (equals-tx
                {:transaction {:merchant "Mer*" :amount 10}}
                {:transaction {:merchant "Mer*" :amount 10}}))))
  (testing "Should return true when tx are equals and merchant with diff cases"
   (is (= true (equals-tx
                {:transaction {:merchant "meR*" :amount 10}}
                {:transaction {:merchant "Mer*" :amount 10}}))))
  (testing "Should return false when tx1 amount diff from tx2"
    (is (= false (equals-tx
                  {:transaction {:merchant "Mer*" :amount 10}}
                  {:transaction {:merchant "Mer*" :amount 5}}))))
  (testing "Should return false when tx1 merchant diff from tx2"
    (is (= false (equals-tx
                  {:transaction {:merchant "AMZ*" :amount 5}}
                  {:transaction {:merchant "Mer*" :amount 5}})))))

(deftest test-similar-tx
  (testing "Should return the filtered similar tx"
    (is (= [{:transaction {:merchant "Mer*" :amount 3}}] (similar-tx
            {:transaction {:merchant "mer*" :amount 3}}
            [{:transaction {:merchant "MER*" :amount 6}}
             {:transaction {:merchant "Amz*" :amount 3}}
             {:transaction {:merchant "Mer*" :amount 3}}]))))
 (testing "Should return an empty vec when no similar found"
   (is (empty? (similar-tx
           {:transaction {:merchant "Allix*" :amount 3}}
           [{:transaction {:merchant "MER*" :amount 6}}
            {:transaction {:merchant "Amz*" :amount 3}}
            {:transaction {:merchant "Mer*" :amount 3}}])))))

(deftest test-doubled-transaction
  (testing "Should return the account itself when authorized is nil"
    (is (= {:account {:availableLimit 10}}
           (doubled-transaction
            {:transaction {:merchant "Mer*" :amount 5}}
            nil
            2
            2
            {:account {:availableLimit 10}}))))
  (testing "Should return the account itself when authorized is empty"
    (is (= {:account {:availableLimit 10}}
           (doubled-transaction
            {:transaction {:merchant "Mer*" :amount 5}}
            []
            2
            2
            {:account {:availableLimit 10}}))))
  (testing "Should return the account itself when there is no similar tx"
    (is (= {:account {:availableLimit 10}}
           (doubled-transaction
            {:transaction {:merchant "Mer*" :amount 5}}
            [{:transaction {:merchant "Mer*" :amount 45 :time (j/zoned-date-time "2019-12-02T10:00:50.000Z")}}
             {:transaction {:merchant "Mer*" :amount 34 :time (j/zoned-date-time "2019-12-02T10:00:00.000Z")}}
             {:transaction {:merchant "Mer*" :amount 33 :time (j/zoned-date-time "2019-12-02T10:00:50.000Z")}}]
            2
            2
            {:account {:availableLimit 10}}))))
  (testing "Should return the account itself when interval is enogh"
    (is (= {:account {:availableLimit 10}}
           (doubled-transaction
            {:transaction {:merchant "Mer*" :amount 5 :time (j/zoned-date-time "2019-12-02T10:03:00.000Z")}}
            [{:transaction {:merchant "Mer*" :amount 5 :time (j/zoned-date-time "2019-12-02T10:00:00.000Z")}}]
            2
            2
            {:account {:availableLimit 10}}))))
  (testing "Should return violation when violate the constraint"
    (is (= {:account {:availableLimit 10} :violations ["doubled-transaction"]}
           (doubled-transaction
            {:transaction {:merchant "Mer*" :amount 5 :time (j/zoned-date-time "2019-12-02T10:02:00.000Z")}}
            [{:transaction {:merchant "MEr*" :amount 5 :time (j/zoned-date-time "2019-12-02T10:01:00.000Z")}}
             {:transaction {:merchant "MEr*" :amount 5 :time (j/zoned-date-time "2019-12-02T10:01:30.000Z")}}]
            2
            2
            {:account {:availableLimit 10}})))))
