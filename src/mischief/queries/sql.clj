(ns mischief.queries.sql
  (:require [honey.sql :as sql]
            [next.jdbc :as jdbc]))

;; (set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;
;; ADMIN QUERIES ;;
;;;;;;;;;;;;;;;;;;;

(def db-time {:select [:current_date]})

(def db-users {:select [:usename]
               :from [:pg_user]})

(def db-schemas {:select [:schema_name]
                 :from [:information_schema.schemata]})

(def db-names {:select [:datname]
               :from [:pg_database]})

;;;;;;;;;;;;;;;;;;;
;; ALTER QUERIES ;;
;;;;;;;;;;;;;;;;;;;

(def characters {:select [:*]
                 :from   [:story.characters]
                 :order-by [[:story.characters.name :asc]]})

(def char-school {:select [:characters.name :characters.school]
                  :from   [:story.characters]
                  :order-by [[:characters.name :asc]]})

;;;;;;;;;
;; SQL ;;
;;;;;;;;;

(defn select
  [db query]
  (jdbc/execute!
   db
   (sql/format query)))
