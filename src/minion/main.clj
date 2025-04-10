#!/usr/bin/env bb

(require '[pod.babashka.postgresql :as pg])
(require '[honey.sql :as hsql])

(def env (into {}
               (map (fn [pair]
                      (let [[k v] (clojure.string/split pair #"=")]
                        [(keyword k) v]))
                    (clojure.string/split (slurp ".env") #"\n"))))

(def db {:dbtype   "postgresql"
         :host     (:POSTGRES_HOST env)
         :dbname   (:POSTGRES_DBNAME env)
         :user     (:POSTGRES_USERNAME env)
         :password (:POSTGRES_PASSWORD env)
         :port     (Integer/parseInt (:POSTGRES_PORT env))})

(let [[command target] *command-line-args*]
  (when (nil? command)
    (prn "No command provided")
    (System/exit 1))

  (case command
    "create" (prn "table-name")
    "update" (prn "table-name")
    (println "Unknown command:"  command)))
