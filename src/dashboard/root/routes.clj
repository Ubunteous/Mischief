(ns dashboard.root.routes
  (:require
   [dashboard.shared-html.core :as page-html]
   [dashboard.system :as-alias system] ;; avoid cycle deps
   [hiccup2.core :as hiccup]
   [honey.sql :as sql]
   [next.jdbc :as jdbc]))

(def sqlmap {:select [:*]
             :from   [:story.chars]})

(defn get-characters
  [db query]
  (jdbc/execute!
   db
   (sql/format sqlmap)))

(defn root-handler
  [{::system/keys [db]} _request]
  ;; (let [{character :name} (get-characters db sqlmap)]
  (let [characters (get-characters db (sql/format sqlmap))]

    {:status 200
     :headers {"Content-Type" "text/html"}

     :body
     (str
      (hiccup/html
       [:h1 (str "Hello, everyone")]
       (page-html/view
        :body
        (for [character characters]
          [:li (:chars/name character) " - " (or (:chars/school character) "No affiliation")]))))}))

;; partial serves to provide system to handlers
;; f(system, request) -> response becomes f(request) -> response
(defn routes
  [system]
  [["/" {:get {:handler (partial #'root-handler system)}}]])
