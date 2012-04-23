
(ns decker.test.core
  (:use decker.core
        midje.sweet))

(facts "about getting connection information"
  (make-connection {:type :mysql :host "localhost" :name "test" :user "root" :pass ""})
    => (contains {:classname "com.mysql.jdbc.Driver"}))

