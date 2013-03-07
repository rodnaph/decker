(ns decker.core
  (:use [clojure.java.jdbc.internal :only [get-connection]])
  (:require [clojure.java.jdbc :as sql]))

(def ^{:dynamic true} config)

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

(defmethod ^{:doc "Creates a PostgresSQL database connection map from the information map specified"}
  make-connection :postgres
  [{:keys [type host user pass name]}]
  { :classname "org.postgresql.Driver"
    :subprotocol "postgresql"
    :subname (format "//%s:3306/%s" host name)
    :user user
    :pass pass })

(def ^{:dynamic true :doc "This is the default page size used by *with-query-results-cursor* which
  is explained next."} *default-fetch-size* 50)

(defn ^{:doc "By default the clojure.java.jdbc *with-query-results* doesn't allow proper lazy evaluation because
  the underlying ResultSet will extract all the data at once.  So this method uses a cursor to fetch
  results a page at a time, but provide a lazy sequence interface.  Taken from http://asymmetrical-view.com/2010/10/14/clojure-lazy-walk-sql-table.html"}
  with-query-results-cursor [[sql & params :as sql-params] func]
  (sql/transaction
   (with-open [stmt (.prepareStatement (sql/connection) sql)]
     (doseq [[index value] (map vector (iterate inc 1) params)]
       (.setObject stmt index value))
     (.setFetchSize stmt *default-fetch-size*)
     (with-open [rset (.executeQuery stmt)]
       (func (sql/resultset-seq rset))))))

;; We also need to take into account the different ways of querying for database meta information.
;; So here we use another multi-method to dispatch on the connection info type to get that.

(defmulti ^{:doc "This function allows applying a function to the name of each table
  in the specified database."}
  with-tables :type)

(defmethod with-tables :mysql
  [info f]
  (sql/with-connection (make-connection info)
    (with-query-results-cursor ["show tables"]
      #(doseq [table %]
         (f (second (first table)))))))

(defmethod with-tables :mssql
  [info f]
  (sql/with-connection (make-connection info)
    (with-query-results-cursor ["select * from INFORMATION_SCHEMA.TABLES"]
      #(doseq [{:keys [table_name]} %]
        (f table_name)))))

;; With database abstraction taken care of we can now handle the actual selection and copying
;; of data from the source to the destination.  Unfortunately the Clojure JDBC library only
;; supports one connection at a time, so we need to read all the data into memory before
;; re-inserting it into the destination.  That's a @todo - should really be handled with a
;; lazy sequence if possible.

(defn ^{:doc "Applies a function for each row in the specified table.  The function receives
  each row in turn as it is fetched."}
  with-rows [info table f]
  (sql/with-connection (make-connection info)
    (with-query-results-cursor [(str "select * from " table)]
      #(doseq [row %] (f row)))))

(defn ^{:doc "Copies the contents of a table from one database to another.  This assumes the
  destination table is empty for now.  The rows are copied lazily so large tables can be
  handled - but performance could be improved."}
  copy-table [from to table]
  (println "Copying table:" table)
  (with-rows from table
    (fn [row]
      (sql/with-connection (make-connection to)
        (sql/insert-records (keyword table) row)))))

(defn ^{:doc "Copies all data from the specified tables from one database to the other."}
  copy-tables [from to tables]
  (doseq [table tables]
    (copy-table from to table)))

(defn ^{:doc "Copy from the source database to the destination one.  The parameters are maps
  containing the connection information for each database. These will then be changed to the
  JDBC connection maps when required."}
  copy [from to]
  (with-tables from
    #(if (not (some #{%} (:exclude-tables from)))
         (copy-table from to %))))

(defn ^{:doc "To run the project just specify a configuration file with the database information
  as detailed in config.clj-sample.  You can then use Leiningen like so:

    lein run /path/to/config.clj

  You will see feedback printed as tables are copied."}
  -main [config-file]
  (load-file config-file)
  (copy (:from config) (:to config)))
