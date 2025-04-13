(ns minion.parts.db
  (:require [clojure.string]
            [honey.sql :as hsql]
            [pod.babashka.postgresql :as pg]))

(set! *warn-on-reflection* true)

(def args
  (let [env (into {}
                  (map (fn [pair]
                         (let [[k v] (clojure.string/split pair #"=")]
                           [(keyword k) v]))
                       (clojure.string/split (slurp ".env") #"\n")))]
    {:dbtype   "postgresql"
     :host     (:POSTGRES_HOST env)
     :dbname   (:POSTGRES_DBNAME env)
     :user     (:POSTGRES_USERNAME env)
     :password (:POSTGRES_PASSWORD env)
     :port     (Integer/parseInt (:POSTGRES_PORT env))}))

(defn table-exists?
  [db table]
  (not (empty?
        (pg/execute! db (hsql/format
                         {:select []
                          :from [:information_schema.tables]
                          :where [:and
                                  [:= :table_schema "story"]
                                  [:= :table_name table]]})))))

(defn select-all
  [db table]
  (try
    (pg/execute! db (hsql/format {:select [:*] :from [(symbol table)]}))
    (catch Exception e
      (println "\nCould not find table" table ". Try one of these instead:\n"
               (map :tables/table_name (pg/execute! db (hsql/format {:select [:table_name]
                                                                     :from [:information_schema.tables]
                                                                     :where [:= :table_schema "story"]})))))))
