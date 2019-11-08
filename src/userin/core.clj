(ns userin.core
  (:gen-class)
  (:require [cheshire.core :refer :all]))

(defn parse [value]
  (parse-string value true))

(defn read-all []
  (loop [line (read-line)]
    (when (and (not (= line "")) (not (= line nil)))
      (println (parse line))
      (recur (read-line)))))

(defn -main
  [& args]
  (read-all))
