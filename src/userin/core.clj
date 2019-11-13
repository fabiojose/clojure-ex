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

;; to check if a given account has an active card
(defn is-card-active [account]
  (if (get-in account [:account :activeCard])
    account
    (assoc account :violations ["card-not-active"])))

;; to check if a given account has limit to process the tx amount
; (defn has-limit [tx account]
;   (if (get-in account [:account :availableLimit])))

;; to check if a given tx should be authorized over an account
(defn authorize [tx account]
  (is-card-active account))

;; to decide what gonna do with parsed json
(defn decide [json account]
  (cond
    (contains? json :account) json
    (contains? json :transaction) (authorize json account)
    :else (throw (Exception. (str "unsupported json: " json)))))

;; to read stdin until end
(defn read-all []
  (loop [line (read-line)]
    (when (is-a-text line)
      (decide (json-parse line))
      (recur (read-line)))))

(defn read-all2 []
  (loop [line (read-line) account nil]
    (when (is-a-text line)
      (println (json-parse line) account)
      (recur (read-line) (if (is-account (json-parse line)) (json-parse line) account) ))))

(defn read-all3 []
  (loop [line (read-line) account nil]
    (when (is-a-text line)
      (println line account)
      (recur (read-line) (decide (json-parse line) account)))))

;; the main function
(defn -main
  [& args]
  (read-all3))
