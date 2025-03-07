(ns mischief.queries.sql
  (:require [honey.sql :as sql]
            [next.jdbc :as jdbc]))

;; (set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;
;; ADMIN QUERIES ;;
;;;;;;;;;;;;;;;;;;;

(def database {:select [:datname]
               :from [:pg_database]})

(def time {:select [:current_date]})

(def users {:select [:usename]
            :from [:pg_user]})

(def schemas {:select [:schema_name]
              :from [:information_schema.schemata]})

(def names {:select [:datname]
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
