(ns userin.core
  (:gen-class)
  (:require [cheshire.core :refer :all])
  (:require [java-time :as j]))

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

(defn print-out
  "write the account as json in the console and return the account"
  [account]
  (println (generate-string (dissoc account :authorized)))
  account)

(defn print-when-violations
  "write the account in the console when violations are present"
  [account]
  (if (has-violations account)
    (print-out account)
    account))

(defn card-active
  "to check if a given account has an active card"
  [account]
  (if (get-in account [:account :activeCard])
    account
    (assoc account :violations ["card-not-active"])))

(defn parse-tx
  "parses a given tx"
  [tx]
  (assoc-in tx [:transaction :time] (j/zoned-date-time (get-in tx [:transaction :time]))))

(defn assoc-tx
  "associates a given tx in the authorized vector of given account"
  [tx account]
  (assoc account :authorized (conj (get account :authorized) tx)))

(defn use-limit
  "to use the limit of given account to process the tx amount"
  [tx account]
  (assoc-in account [:account :availableLimit] (- (limit-of account) (amount-of tx))))

(defn sufficient-limit
  "to check if a given account has sufficient limit to process the given tx"
  [tx account]
  (if (not (contains? account :violations))
    (if (< (limit-of account) (amount-of tx))
      (assoc account :violations ["insufficient-limit"])
      (assoc-tx tx (use-limit tx account)))
    account))

(defn get-it-reverse
  "to get a item from a given list based on the index (starting from zero), reversely"
  [items index]
  (if (< (count items) index)
    nil
    (get-in (vec (reverse items)) [index])))

(defn time-diff-tx
  "to return the elapsed time between two transactions"
  [txa txb]
  (if (or (nil? txa) (nil? txb))
    nil
    (j/duration (get-in txa [:transaction :time]) (get-in txb [:transaction :time]))))

(defn as-minutes
  "to return the minutes of given duration"
  [duration alternative]
  (if (nil? duration)
    alternative
    (j/as duration :minutes)))

(defn high-frequency
  "to check if a given tx and authorized, meets high-frequency-small-interval"
  [tx authorized frequency interval account]
  (if (<= (as-minutes (time-diff-tx (get-it-reverse authorized (- frequency 2)) tx) (+ interval 1)) interval)
    (assoc account :violations ["high-frequency-small-interval"])
    account))

(defn equals-tx
  "to return true if the given tx are equals (same merchant and amount)"
  [tx1 tx2]
  (and (= (.toLowerCase (-> tx1 :transaction :merchant))
          (.toLowerCase (-> tx2 :transaction :merchant)))

       (= (-> tx1 :transaction :amount)
          (-> tx2 :transaction :amount))))

(defn similar-tx
  "to return the authorized transactions similar of given tx"
  [tx authorized]
  (filter (partial equals-tx tx) authorized))

(defn doubled-transaction
  "to check if a given tx meets the doubled-transaction violation"
  [tx authorized frequency interval account]
  (if (<= (as-minutes (time-diff-tx (get-it-reverse (similar-tx tx authorized) (- frequency 1)) tx) (+ interval 1)) interval)
    (assoc account :violations ["doubled-transaction"])
    account))

(defn authorize
  "to check if a given tx should be authorized over an account"
  [tx account]
  (sufficient-limit tx (high-frequency tx (get account :authorized) 3 2 (card-active account))))

(defn already-initialized
  "to check if we already have an initialized account"
  [json account]
  (if (get-in account [:account :activeCard])
    (assoc account :violations ["account-already-initialized"])
    json))

(defn decide
  "to decide what gonna do with parsed json"
  [json account]
  (cond
    (contains? json :account) (already-initialized json account)
    (contains? json :transaction) (authorize (parse-tx json) account)
    :else (throw (Exception. (str "unsupported json: " json)))))

(defn read-all
  "to read stdin until its end"
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
