(defproject userin "0.1.0-SNAPSHOT"
  :description "App to read stdin"
  :url "https://github.com/fabiojose/clojure-ex"
  :license {:name "Apache-2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cheshire "5.9.0"]]
  :main userin.core
  :profiles {:uberjar {:aot :all}}
  :uberjar-name "app.jar")
