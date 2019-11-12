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
