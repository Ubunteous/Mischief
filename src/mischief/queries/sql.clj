(ns mischief.queries.sql
  (:require [clojure.string :as string]
            [hiccup2.core :as hiccup]
            [honey.sql :as sql]
            [incanter.charts :as chart]
            [incanter.core :as graph]
            [mischief.shared-html.core :as page-html]
            [next.jdbc :as jdbc])

  ;; (:import (java.io FileOutputStream
  ;;                   ByteArrayOutputStream
  ;;                   ByteArrayInputStream))
  )

;; (set! *warn-on-reflection* true)

(def db-users {:select [:usename]
               :from [:pg_user]})

(def db-schemas {:select [:schema_name]
                 :from [:information_schema.schemata]})

(def db-names {:select [:datname]
               :from [:pg_database]})

(def characters {:select [:*]
                 :from   [:story.characters]
                 :order-by [[:story.characters.name :asc]]})

(def char-school {:select [:characters.name :characters.school]
                  :from   [:story.characters]
                  :order-by [[:characters.name :asc]]})

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
     "html, body {height: 100%;}\n
      html {display: table; margin: auto;}\n
      body {display: table-cell; vertical-align: middle;}\n
      table, th, td {border: 1px solid black;}\n
      h1 {color: blue;}"]

    [:h1 (str "Table")]

    [:table
     [:tbody
      ;; categories
      [:tr
       (for [categories (map symbol (keys (first rows)))]
         (into [:th]
               (map string/capitalize
                    (rest (string/split (str categories) #"/")))))]
      ;; content
      (for [row rows]
        (into [:tr]
              (map #(into [:th] (str %))
                   (replace {nil "null"} (vals row)))))]])))

(defn to-html-graph
  [rows]
  (let [axis (for [cols (keys (first rows))] (map cols rows))]

    ;; (graph/view
    (graph/save
     (chart/bar-chart
      (first axis)
      (second axis))
     ;; (FileOutputStream. "res/myplot.png"))
     "res/myplot.png")

    (str
     (hiccup/html
      [:h1 (str "Image")]
      [:img {:src "/myplot.png"}]))))
