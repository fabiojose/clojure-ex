(ns userin.core
  (:gen-class)
  (:require [cheshire.core :refer :all]))

(defn json-parse
  "to parse given value as json"
  [value]
  (parse-string value true))

(defn is-a-text
  "to check if a given arg is a valid text"
  [text]
  (and (not (= text "")) (not (= text nil))))

(defn is-account
  "to check if a given json is an account object"
  [json]
  (contains? json :account))

(defn limit-of
  "to get the available limit of given account"
  [account]
  (get-in account [:account :availableLimit]))

(defn amount-of
  "to get the amount to given transaction"
  [tx]
  (get-in tx [:transaction :amount]))

(defn has-violations
  "to check if a given account has violations"
  [account]
  (contains? account :violations))

(defn print-and-throw
  "to print the account and throw the exception"
  [account]
  (println (generate-string account))
  (throw (Exception. "violations found")))

(defn print-when-violations
  "write at the console when violations are present"
  [account]
  (if (has-violations account)
    (println (generate-string account))
    account))

(defn is-card-active
  "to check if a given account has an active card"
  [account]
  (if (get-in account [:account :activeCard])
    account
    (assoc account :violations ["card-not-active"])))

(defn assoc-tx
  "associates a given tx in the authorized vector of given account"
  [tx account]
  (assoc account :authorized (conj (get account :authorized) tx)))

(defn use-limit
  "to use the limit of given account to process the tx amount"
  [tx account]
  (assoc-in account [:account :availableLimit] (- (limit-of account) (amount-of tx)))
  (assoc-tx tx account))

(defn sufficient-limit
  "to check if a given account has sufficient limit to process the given tx"
  [tx account]
  (if (< (limit-of account) (amount-of tx))
    (assoc account :violations ["insufficient-limit"])
    (use-limit tx account)))

(defn authorize
  "to check if a given tx should be authorized over an account"
  [tx account]
  (sufficient-limit tx (is-card-active account)))

(defn already-initialized
  "to check if we already have an initialized account"
  [json account]
  (if (contains? account :account)
    (assoc account :violations ["account-already-initialized"])
    json))

(defn decide
  "to decide what gonna do with parsed json"
  [json account]
  (cond
    (contains? json :account) (already-initialized json account)
    (contains? json :transaction) (authorize json account)
    :else (throw (Exception. (str "unsupported json: " json)))))

(defn read-all
  "to read stdin until it ends"
  []
  (loop [line (read-line) account nil]
    (println account)
    (when (is-a-text line)
      ;;(println line account)
      (recur (read-line) (print-when-violations (decide (json-parse line) account))))))

(defn -main
  "the main function"
  [& args]
  (read-all))
