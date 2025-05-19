(ns minion.parts.db
  (:require [clojure.set]
            [clojure.string :as str]
            [honey.sql :as hsql]
            [minion.parts.file-io :as fio]
            [pod.babashka.postgresql :as pg]))

(set! *warn-on-reflection* true)

;;;;;;;;;;;
;; SETUP ;;
;;;;;;;;;;;

(def args
  {:dbtype   "postgresql"
   :host     (:POSTGRES_HOST fio/env)
   :dbname   (:POSTGRES_DBNAME fio/env)
   :user     (:POSTGRES_USERNAME fio/env)
   :password (:POSTGRES_PASSWORD fio/env)
   :port     (Integer/parseInt (:POSTGRES_PORT fio/env))})

;; (defn table-exists?
;;   [table]
;;   (boolean (seq
;;             (pg/execute! args (hsql/format
;;                                {:select []
;;                                 :from [:information_schema.tables]
;;                                 :where [:and
;;                                         [:= :table_schema "story"]
;;                                         [:= :table_name table]]})))))

;;;;;;;;;
;; GET ;;
;;;;;;;;;

(defn select-raw
  [query]
  (pg/execute! args query))

(defn select
  [query]
  (pg/execute! args (hsql/format query)))

(defn select-tables
  []
  (select {:select [:table_name]
           :from [:information_schema.tables]
           :where [:= :table_schema "story"]}))

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

;;;;;;;;;;;;
;; SELECT ;;
;;;;;;;;;;;;

(defn id-to-foreign-name
  [table]
  (let [foreign-data (select {:select [:id :name] :from [(symbol table)]})]
    (apply merge (map
                  (fn [m] (apply hash-map (vals m)))
                  foreign-data))))

(defn replace-foreign-key [table foreign-col query]
  (let [foreign-key (keyword table (name foreign-col))]
    (map (fn [row]
           (assoc row foreign-key
                  ((id-to-foreign-name (name foreign-key))
                   (row foreign-key))))
         query)))

(defn replace-foreign-keys [query table]
  (reduce
   (fn [acc key] (replace-foreign-key table key acc))
   query
   (get-foreign-cols table)))

(defn select-all
  [table]
  (try
    (let [query (pg/execute! args (hsql/format {:select [:*] :from [(symbol table)] :order-by [:id]}))]
      (replace-foreign-keys query table))

    (catch Exception e
      (println "\nError:" e "\nCould not find table" table ". Try one of these instead:\n"
               (map :tables/table_name
                    (select-tables))))))

;;;;;;;;;;;;
;; UPDATE ;;
;;;;;;;;;;;;

(defn minimise-table-iterator
  [table-name]
  (let [[{next-seq-id :max}] (select {:select [:%max.id] :from [(keyword table-name)]})]
    (when next-seq-id
      (select-raw [(str "ALTER SEQUENCE " table-name "_id_seq RESTART WITH " (inc next-seq-id))]))))

(defn name-to-foreign-id
  [table]
  (clojure.set/map-invert (id-to-foreign-name table)))

(defn swap-foreign-with-id [foreign-key query]
  (map (fn [row]
         (assoc row foreign-key
                ((name-to-foreign-id (name foreign-key))
                 (row foreign-key))))
       query))

(defn swap-foreign-with-ids [query table]
  (reduce
   (fn [acc key] (swap-foreign-with-id key acc))
   query
   (get-foreign-cols table)))

(defn upsert
  [table query]
  (let [cols (into [] (keys (first query)))]
    (pg/execute! args
                 (hsql/format
                  {:insert-into [(symbol table)]
                   :values query
                   :on-conflict :name :do-update-set cols
                   :returning :*}))))

(defn cross-upsert
  [table query]
  (let [[first-col second-col] (str/split table #"x")
        [first-kw second-kw] (map keyword [first-col second-col])]

    (pg/execute! args
                 [(str "INSERT INTO " table
                       " (" first-col ", " second-col ")"
                       " VALUES "
                       (str/join ","
                                 (map (fn [q]
                                        (str "(" (first-kw q) "," (second-kw q) ")"))
                                      query))
                       " ON CONFLICT (" first-col ", " second-col
                       ") DO NOTHING RETURNING *;")])))

;;;;;;;;;;;;;;;;;
;; PRE-PROCESS ;;
;;;;;;;;;;;;;;;;;

(defn get-cols-types
  [table]
  (apply merge
         (map #(hash-map (keyword (:columns/column_name %)) (:columns/data_type %))
              (select
               {:select [:column_name :data_type]
                :from [:information_schema.columns]
                :where [:= :table_name table]}))))

(defn get-cols-type-converter
  [col-types]
  (let [m (dissoc col-types :id)
        f #(case %
             "integer" (fn [s] (if (re-matches #"\d+" s)
                                 (Integer/parseInt s)
                                 (str/trim s)))
             "boolean" (fn [s] (case s "true" true "false" false))
             "character varying" str/trim
             (fn [_s] (str "Type unknown: " %)))]
    (into {} (map (fn [[k v]] [k (f v)]) m))))

(defn convert-col-types [func-map value-map]
  (try
    (into {} (map (fn [[k f]] [k (f (get value-map k))])
                  func-map))
    (catch Exception e
      (println "Error:" (.getMessage e)
               "\n with keys" (keys func-map)
               "\n not matching " value-map
               "\n Difference:" (keys (apply dissoc func-map (keys value-map)))))))
