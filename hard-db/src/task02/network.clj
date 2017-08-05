(ns task02.network
  (:use [task02 helpers query])
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.net Socket ServerSocket InetAddress InetSocketAddress SocketTimeoutException]))


(def ^:private should-be-finished (promise))


(defn handle-request [^Socket sock]
  (binding [*out* (io/writer sock)
            *in* (io/reader sock)]
    (try
      (let [s (read-line)]
        (if (= (str/lower-case s) "quit")
          (deliver should-be-finished true)
          (println (perform-query s))))
      (catch Throwable ex
        (println "Exception: " ex))
      (finally
        (.close sock)))))


(defn- run-loop [server-sock]
  (try
    (let [^Socket sock (.accept server-sock)]
      (future (handle-request sock)))
    (catch SocketTimeoutException ex)
    (catch Throwable ex
      (println "Got exception" ex)
        (deliver should-be-finished true))))


(defn run [port]
  (let [sock-addr (InetSocketAddress. "localhost" port)
        server-socket (doto (ServerSocket.)
                        (.setReuseAddress true)
                        (.setSoTimeout 3000)
                        (.bind sock-addr))]
    (loop [_ (run-loop server-socket)]
      (when-not (realized? should-be-finished) ;; следующий запрос если работа не завершается...
        (recur (run-loop server-socket))))
    (.close server-socket)))
