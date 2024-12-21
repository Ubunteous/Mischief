(ns dashboard.queries.sql
  (:require [clojure.string :as string]
            [dashboard.shared-html.core :as page-html]
            [hiccup2.core :as hiccup]
            [honey.sql :as sql]
            [incanter.charts :as chart]
            [incanter.core :as graph]
            [next.jdbc :as jdbc])

  ;; (:import (java.io FileOutputStream
  ;;                   ByteArrayOutputStream
  ;;                   ByteArrayInputStream))
  )

;; (set! *warn-on-reflection* true)

(def characters {:select [:*]
                 :from   [:story.chars]
                 :order-by [[:chars.name :asc]]})

(def char-school {:select [:chars.name :chars.school]
                  :from   [:story.chars]
                  :order-by [[:chars.name :asc]]})

(def char-age {:select [:chars.name :chars.age]
               :from   [:story.chars]
               :order-by [[:chars.name :asc]]})

(defn select
  [db query]
  (jdbc/execute!
   db
   (sql/format query)))

(defn hello
  []
  (str
   (hiccup/html
    (page-html/view)
    [:h1 (str "Hello")])))

(defn to-html-list
  [rows]
  (str
   (hiccup/html
    (page-html/view)
    [:h1 (str "List")]

    (for [row rows]
      [:li (string/join
            " - "
            (remove nil? (vals row)))]))))

(defn to-html-table
  [rows]
  (str
   (hiccup/html
    (page-html/view)
    [:style
     "table, th, td {border: 1px solid black;} \nh1 {color: blue;}"]

    [:h1 (str "List")]

    [:table
     [:tbody

      ;; categories
      [:tr
       (for [row (map symbol (keys (first rows)))]
         (into [:th] (map string/capitalize (rest (string/split (str row) #"/")))))]

      ;; content
      (for [row rows]
        [:tr
         (for [r row]
           (into [:th] (or (last r) "null")))])]])))
