
(ns decker.test.core
  (:use decker.core
        midje.sweet))

(facts "about default database information"
  (db-info {:user "another"}) => (contains {:type :mysql :host "localhost" :user "another" :pass ""})
  (db-info {}) => (contains {:type :mysql :host "localhost" :user "root" :pass ""}))

