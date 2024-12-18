(ns user
  (:require [dashboard.system :as system]))

(def system nil)

(defn start-system!
  []
  (if system
    (println "Already Started")
    (alter-var-root #'system (constantly (system/start-system)))))

(defn stop-system!
  []
  (if-not system
    (println "No system found")
    (do
      (system/stop-system system)
      (alter-var-root #'system (constantly nil)))))

(defn restart-system!
  []
  (if-not system
    (println "No system found")
    (do
      (stop-system!)
      (start-system!))))

(defn server
  []
  (::system/server system))

(defn db
  []
  (::system/db system))

(defn env
  []
  (::system/env system))
