(ns userin.core-test
  (:require [clojure.test :refer :all]
            [userin.core :refer :all]))

(deftest test-is-a-text
  (testing "Should return false when text is not valid"
    (is (= (is-a-text "") false))
    (is (= (is-a-text nil) false))))
