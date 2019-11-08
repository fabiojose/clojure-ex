(ns userin.core
  (:gen-class)
  (:require [cheshire.core :refer :all]))

(defn parse [value]
  (parse-string value true))

(defn is-a-text [text]
  (and (not (= text "")) (not (= text nil))))

(defn read-all []
  (loop [line (read-line)]
    (when (is-a-text line)
      (println (parse line))
      (recur (read-line)))))

(defn -main
  [& args]
  (read-all))
