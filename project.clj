
(defproject rodnaph/decker "0.2.0"
  :description "Easy database copying"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/java.jdbc "0.1.4"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [com.microsoft/sqljdbc4 "3.0"]]
  :dev-dependencies [[lein-marginalia "0.7.0"]
                     [midje "1.3.1"]
                     [lein-midje "1.0.9"]]
  :main decker.core)

