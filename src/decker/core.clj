
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

(defn ^{:doc "Creates a database connection map from the information map
              specified."}
  cnn [{:keys [type host user pass name]}]
  { :classname "com.mysql.jdbc.Driver"
    :subprotocol "mysql"
    :subname (format "//%s:3306/%s" host name)
    :user user
    :pass pass })

(defn ^{:doc "Returns all the rows from a specified table."}
  rows-from [info table]
  (sql/with-connection (cnn info)
    (sql/with-query-results rows
      [(str "select * from " table)]
      (doall (map #(first %) rows)))))

(defn ^{:doc "Copies the contents of a table from one database to another.
              This assumes the destination table is empty for now."}
  copy-table [table from to]
  (println "Copying table:" table)
  (let [rows (rows-from from table)]
    (sql/with-connection (cnn to)
      (sql/insert-records table rows))))

(defn ^{:doc "Returns the names of all the tables in the database. These
              are not in any specific order."}
  tables [info]
  (sql/with-connection (cnn info)
    (sql/with-query-results rows
      ["show tables"]
      (doall (map #(:tables_in_tasks %) rows)))))

(defn ^{:doc "Copy from the source database to the destination one.
              The parameters are maps containing the connection information
              for each database."}
  copy [from to]
  (let [db-from (db-info from)]
    (doseq [table (tables db-from)]
      (copy-table table db-from (db-info to)))))

(defn -main [])

