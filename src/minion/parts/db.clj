(ns minion.parts.db
  (:require [clojure.string]
            [honey.sql :as hsql]
            [pod.babashka.postgresql :as pg]))

(set! *warn-on-reflection* true)

;;;;;;;;;;;
;; SETUP ;;
;;;;;;;;;;;

(def env (into {}
               (map (fn [pair]
                      (let [[k v] (clojure.string/split pair #"=")]
                        [(keyword k) v]))
                    (clojure.string/split (slurp ".env") #"\n"))))

(def res-path (:RES_PATH env))

(def args
  {:dbtype   "postgresql"
   :host     (:POSTGRES_HOST env)
   :dbname   (:POSTGRES_DBNAME env)
   :user     (:POSTGRES_USERNAME env)
   :password (:POSTGRES_PASSWORD env)
   :port     (Integer/parseInt (:POSTGRES_PORT env))})

(defn table-exists?
  [table]
  (boolean (seq
            (pg/execute! args (hsql/format
                               {:select []
                                :from [:information_schema.tables]
                                :where [:and
                                        [:= :table_schema "story"]
                                        [:= :table_name table]]})))))

;;;;;;;;;;;;
;; SELECT ;;
;;;;;;;;;;;;

(defn select
  [query]
  (pg/execute! args (hsql/format query)))

(defn upsert
  [table query]
  (let [cols (into [] (keys (first query)))]
    (pg/execute! args
                 (hsql/format
                  {:insert-into [(symbol table)]
                   :values query
                   :on-conflict :name :do-update-set cols
                   :returning :*}))))

(defn update
  [table query]
  (pg/execute! args
               (hsql/format
                {:update [(symbol table)]
                 :set query
                 :on-conflict :name :do-update-set :name
				 ;; :on-duplicate-key-update :*
                 :returning :*})))

(defn select-all
  [table]
  (try
    (pg/execute! args (hsql/format {:select [:*] :from [(symbol table)]}))
    (catch Exception e
      (println "\nCould not find table" table ". Try one of these instead:\n"
               (map :tables/table_name
                    (pg/execute! args (hsql/format {:select [:table_name]
                                                    :from [:information_schema.tables]
                                                    :where [:= :table_schema "story"]})))))))

;;;;;;;;;
;; GET ;;
;;;;;;;;;

(defn get-foreign-cols
  [table]
  (map #(keyword (:key_column_usage/column_name %))
       (select {:select [:information_schema.key_column_usage.column_name]
                :from [:information_schema.table_constraints]
                :join
                [:information_schema.key_column_usage
                 [:=
                  :information_schema.key_column_usage.constraint_name
                  :information_schema.table_constraints.constraint_name]]
                :where
                [:and
                 [:= :information_schema.table_constraints.constraint_type "FOREIGN KEY"]
                 [:= :information_schema.table_constraints.table_schema "story"]
                 [:= :information_schema.table_constraints.table_name table]]})))

(defn get-cols-types
  [table]
  (apply merge
         (map #(hash-map (keyword (:columns/column_name %)) (:columns/data_type %))
              (select
               {:select [:column_name :data_type]
                :from [:information_schema.columns]
                :where [:= :table_name table]}))))

;;;;;;;;;;;;;;;;;
;; PRE-PROCESS ;;
;;;;;;;;;;;;;;;;;

(defn get-cols-converter
  [col-types]
  (let [m (dissoc col-types :id)
        f #(case %
             "integer" (fn [s] (if (re-matches #"\d+" s) (Integer/parseInt s) s))
             "boolean" (fn [s] (case s "true" true "false" false))
             "character varying" (fn [s] s))]
    (into {} (map (fn [[k v]] [k (f v)]) m))))

(defn convert-columns [func-map value-map]
  (into {} (map (fn [[k f]]
                  [k (f (get value-map k))])
                func-map)))

(defn replace-foreign-keys [hash-maps keys]
  (map (fn [m]
         (reduce (fn [acc k]
                   (if (contains? acc k)
                     (assoc acc k {:select [:id] :from [k] :where [:= :name (str (k acc))]})
                     acc))
                 m
                 keys))
       hash-maps))
