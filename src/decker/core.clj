
(ns decker.core
  (:use [clojure.java.jdbc.internal :only [get-connection]])
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

(defn cnn [{:keys [type host user pass name]}]
  { :classname "com.mysql.jdbc.Driver"
    :subprotocol "mysql"
    :subname (format "//%s:3306/%s" host name)
    :user user
    :pass pass })

(copy {:name "tasks"} {:name "taskscopy"})

(rows-from (db-info {:name "tasks"}) "task")

(defn rows-from [info table]
  (sql/with-connection (cnn info)
    (sql/with-query-results rows
      ["select * from task"]
      (doall (map identity rows)))))

(defn copy-table 
  [table from to]
  (prn "Copying " table)
  (let [rows (rows-from info table)]
    (sql/with-connection (cnn to)
      (sql/insert-records table rows))))

(defn tables [info]
  (sql/with-connection (cnn info)
    (sql/with-query-results rows
      ["show tables"]
      (doall (map #(:tables_in_tasks %) rows)))
  ))

(defn ^{:doc "Copy from the source database to the destination one.
              The parameters are maps containing the connection information
              for each database."}
  copy [from to]
  (let [db-from (db-info from)]
    (doseq [table (tables db-from)]
      (copy-table table db-from (db-info to)))))

;; Main

(defn -main [])

