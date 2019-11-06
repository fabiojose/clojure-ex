(ns userin.core
  (:gen-class))

(defn read-all []
  (loop [line (read-line)]
    (when (not (= line ""))
      (println line)
      (recur (read-line)))))

(defn -main
  [& args]
  (read-all))
