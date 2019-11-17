(ns userin.core
  (:gen-class)
  (:require [cheshire.core :refer :all]))

;; to parse given value as json
(defn json-parse [value]
  (parse-string value true))

;; to check if a given arg is a valid text
(defn is-a-text [text]
  (and (not (= text "")) (not (= text nil))))

;; to check if a given json is an account object
(defn is-account [json]
  (contains? json :account))

;; to get the available limit of given account
(defn limit-of [account]
  (get-in account [:account :availableLimit]))

;; to get the amount to given transaction
(defn amount-of [tx]
  (get-in tx [:transaction :amount]))

;; to check if a given account has violations
(defn has-violations [account]
  (contains? account :violations))

;; to print the account and throw the exception
(defn print-and-throw [account]
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

; (defn use-limit
;   "to check if a given account has limit to process the tx amount and use it"
;   [tx account]
;   (if (< (limit-of account) (amount-of tx))
;     (assoc account :violations ["insufficient-limit"])
;     (assoc-in account [:account :availableLimit] (- (limit-of account) (amount-of tx))) ))

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

;; to check if we alread have an initialized account
(defn already-initialized [json account]
  (if (contains? account :account)
    (assoc account :violations ["account-already-initialized"])
    json))

;; to decide what gonna do with parsed json
(defn decide [json account]
  (cond
    (contains? json :account) (already-initialized json account)
    (contains? json :transaction) (authorize json account)
    :else (throw (Exception. (str "unsupported json: " json)))))

;; to read stdin until end
(defn read-all []
  (loop [line (read-line) account nil]
    (println account)
    (when (is-a-text line)
      ;;(println line account)
      (recur (read-line) (print-when-violations (decide (json-parse line) account))))))

;; the main function
(defn -main
  [& args]
  (read-all))
