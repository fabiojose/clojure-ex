(ns userin.core
  (:gen-class)
  (:require [cheshire.core :refer :all]))

(defn json-parse [value]
  (parse-string value true))

(defn is-a-text [text]
  (and (not (= text "")) (not (= text nil))))

(defn read-all []
  (loop [line (read-line)]
    (when (is-a-text line)
      (println (json-parse line))
      (recur (read-line)))))

(defn -main
  [& args]
  (read-all))
