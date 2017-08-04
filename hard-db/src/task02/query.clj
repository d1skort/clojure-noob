(ns task02.query
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as str])
  (:use [task02 helpers db]))


(def operator-map
  { "!=" not=
    "=" =
    "<" <
    ">" >
    "<=" <=
    ">=" >= })


(defn- str->int [value]
  (try
    (parse-int value)
    (catch NumberFormatException e value)))


(defn make-where-function
  [column comp-op value]
  (fn [record]
    ((operator-map comp-op) ((keyword column) record) (str->int value))))


(defn- parse-query [query]
  (match query
    ["select" table & other]
         (->> (parse-query other)
              (into [table]))
    ["where" column comp-op value & other]
         (->> (parse-query other)
              (into [:where (make-where-function column comp-op value)]))
    ["order" "by" column & other]
         (->> (parse-query other)
              (into [:order-by (keyword column)]))
    ["limit" n & other]
         (->> (parse-query other)
              (into [:limit (parse-int n)]))
    ["join" other-table "on" left-column "=" right-column & other]
         (->> (parse-query other)
              (into [:joins [[(keyword left-column) other-table (keyword right-column)]]]))
    :else nil))


(defn parse-select [^String sel-string]
  (-> sel-string
      str/lower-case
      (str/split #" ")
      parse-query
      seq))


(defn perform-query [^String sel-string]
  (if-let [query (parse-select sel-string)]
    (apply select (get-table (first query)) (rest query))
    (throw (IllegalArgumentException. (str "Can't parse query: " sel-string)))))
