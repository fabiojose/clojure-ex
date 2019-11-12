(ns userin.core
  (:gen-class)
  (:require [cheshire.core :refer :all]))

;; to parse given value as json
(defn json-parse [value]
  (parse-string value true))

;; to check if a given arg is a valid text
(defn is-a-text [text]
  (and (not (= text "")) (not (= text nil))))

;; to decide what to do
(defn decide [json]
  (cond
    (contains? json :account) (println "account")
    (contains? json :transaction) (println "transaction")
    :else (throw (Exception. (str "unsupported json: " json)))))

;; to read stdin until end
(defn read-all []
  (loop [line (read-line)]
    (when (is-a-text line)
      (decide (json-parse line))
      (recur (read-line)))))

;; the main function
(defn -main
  [& args]
  (read-all))
