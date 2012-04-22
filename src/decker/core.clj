
(ns decker.core
  (:require [clojure.java.jdbc :as sql]))

(def db-defaults {
  :type :mysql
  :host "localhost"
  :user "root"
  :pass ""
})

(defn db-info
  [info]
  (merge db-defaults info))

;; Public

;(copy {:name "tasks"} {:name "taskscopy"})

(defn ^{:doc "Copy from the source database to the destination one.
              The parameters are maps containing the connection information
              for each database."}
  copy [from to])

;; Main

(defn -main [])

