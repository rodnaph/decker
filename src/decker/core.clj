
(ns decker.core
  (:use [clojure.java.jdbc.internal :only [get-connection]])
  (:require [clojure.java.jdbc :as sql]))

;; This multi-method handles taking a map of connection information and
;; creating a JDBC connection map from it.

(defmulti ^{:doc "Takes some database connection information and returns the JDBC connection info"}
  make-connection :type)

(defmethod ^{:doc "Create a MSSQL database connection"}
  make-connection :mssql
  [{:keys [type host user pass name]}]
  { :classname "com.microsoft.sqlserver.jsbc.SQLServerDriver"
    :subprotocol "sqlserver"
    :subname (format "//%s:1433;databaseName=%s" host name) 
    :user user
    :password pass })

(defmethod ^{:doc "Creates a MySQL database connection map from the information map specified."}
  make-connection :mysql 
  [{:keys [type host user pass name]}]
  { :classname "com.mysql.jdbc.Driver"
    :subprotocol "mysql"
    :subname (format "//%s:3306/%s" host name)
    :user user
    :pass pass })

;; We also need to take into account the different ways of querying for database meta information.
;; So here we use another multi-method to dispatch on the connection info type to get that.

(defmulti get-tables :type)

(defmethod get-tables :mysql
  [info]
  (sql/with-connection (make-connection info)
    (sql/with-query-results rows
      ["show tables"]
      (doall (map #(second (first %)) rows)))))

(defmethod get-tables :mssql
  [info]
  (sql/with-connection (make-connection info)
    (sql/with-query-results rows
      ["select * from INFORMATION_SCHEMA.TABLES "]
      (doall (map #(:table_name %) rows)))))

;; With database abstraction taken care of we can now handle the actual selection and copying
;; of data from the source to the destination.  Unfortunately the Clojure JDBC library only
;; supports one connection at a time, so we need to read all the data into memory before
;; re-inserting it into the destination.  That's a @todo - should really be handled with a
;; lazy sequence if possible.

(defn ^{:doc "Returns all the rows from a specified table."}
  rows-from [info table]
  (sql/with-connection (make-connection info)
    (sql/with-query-results rows
      [(str "select * from " table)]
      (doall (map identity rows)))))

(defn ^{:doc "Copies the contents of a table from one database to another.
              This assumes the destination table is empty for now."}
  copy-table [from to table]
  (let [rows (rows-from from table)]
    (sql/with-connection (make-connection to)
      (apply (partial sql/insert-records (keyword table)) rows))))

(defn ^{:doc "Copy from the source database to the destination one.
              The parameters are maps containing the connection information
              for each database."}
  copy [from to]
  (let [db-from (db-info from)]
    (doseq [table (tables db-from)]
      (copy-table table db-from (db-info to)))))

(defn -main [])

