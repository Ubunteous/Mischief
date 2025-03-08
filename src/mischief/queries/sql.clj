(ns mischief.queries.sql
  (:require [honey.sql :as sql]
            [next.jdbc :as jdbc]))

;; (set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;
;; ADMIN QUERIES ;;
;;;;;;;;;;;;;;;;;;;

(def database {:select [:datname]
               :from [:pg_database]})

(def dbtime {:select [:current_date]})

(def users {:select [:usename]
            :from [:pg_user]})

(def schemas {:select [:schema_name]
              :from [:information_schema.schemata]})

(def names {:select [:datname]
            :from [:pg_database]})

(def tables {:select [:schemaname :tablename :tableowner]
             :from [:pg_catalog.pg_tables]
             :where [:= :schemaname "story"]})

(def columns {:select [:table_name :column_name
                       :column_default :udt_name]
              :from [:information_schema.columns]
              :where [:= :table_schema "story"]})

(defn table-columns [table]
  {:select [:column_name :column_default :udt_name]
   :from [:information_schema.columns]
   :where [:= :table_name table]})

;;;;;;;;;;;;;;;;;;;
;; ALTER QUERIES ;;
;;;;;;;;;;;;;;;;;;;

(def characters {:select [:*]
                 :from   [:story.characters]
                 :order-by [[:story.characters.name :asc]]})

;; (def char-school {:select [:story.characters.name :characters.school]
;;                   :from   [:story.characters]
;;                   :order-by [[:characters.name :asc]]})

(def ages {:select [:story.characters.name :story.characters.age]
           :from [:story.characters]})

;;;;;;;;;
;; SQL ;;
;;;;;;;;;

(defn select
  [db query]
  (jdbc/execute!
   db
   (sql/format query)))

(defn bin
  [db]

  (let [tables-names
        (for [row (select db tables)]
          (:pg_tables/tablename row))]

    (for [table tables-names]
      {:name table
       :content (select db (table-columns table))})))
