(ns minion.parts.db
  (:require [clojure.set]
            [clojure.string]
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

(defn select
  [query]
  (pg/execute! args (hsql/format query)))

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
   (fn [acc key]
     (replace-foreign-key table key acc))
   query
   (get-foreign-cols table)))

(defn select-all
  [table]
  (try
    (let [query (pg/execute! args (hsql/format {:select [:*] :from [(symbol table)]}))]
      (replace-foreign-keys query table))

    (catch Exception e
      (println "\nError:" e "\nCould not find table" table ". Try one of these instead:\n"
               (map :tables/table_name
                    (pg/execute! args (hsql/format {:select [:table_name]
                                                    :from [:information_schema.tables]
                                                    :where [:= :table_schema "story"]})))))))

;;;;;;;;;;;;
;; UPDATE ;;
;;;;;;;;;;;;

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
   (fn [acc key]
     (swap-foreign-with-id key acc))
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

;;;;;;;;;;;;;;;;;
;; PRE-PROCESS ;;
;;;;;;;;;;;;;;;;;

(defn get-cols-type-converter
  [col-types]
  (let [m (dissoc col-types :id)
        f #(case %
             "integer" (fn [s] (if (re-matches #"\d+" s) (Integer/parseInt s) s))
             "boolean" (fn [s] (case s "true" true "false" false))
             "character varying" clojure.string/trim)]
    (into {} (map (fn [[k v]] [k (f v)]) m))))

(defn convert-col-types [func-map value-map]
  (into {} (map (fn [[k f]]
                  [k (f (get value-map k))])
                func-map)))
