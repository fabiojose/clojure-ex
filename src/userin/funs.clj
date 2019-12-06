(ns userin.funs
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

(defn get-it-reverse
  "to get a item from a given list based on the index (starting from zero), reversely"
  [items index]
  (if (< (count items) index)
    nil
    (get-in (vec (reverse items)) [index])))
