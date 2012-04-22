
(ns decker.core
  (:require [clojure.java.jdbc :as sql]))

(defn ^{:doc "Copy from the source database to the destination one.
              The parameters are maps containing the connection information
              for each database."}
  copy [from to])

;; Main

(defn -main [])

