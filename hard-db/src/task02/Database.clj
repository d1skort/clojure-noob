(ns task02.Database
  (:use [task02 db query])
  (:gen-class))


(defn ^:static -InitDatabase []
  (load-initial-data))


(defn ^:static -Select ^String [^String query]
  (pr-str (perform-query query)))
