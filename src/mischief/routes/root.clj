(ns mischief.routes.root
  (:require
   [mischief.presentation.html :as presentation]
   [mischief.queries.sql :as query]
   [mischief.system :as-alias system] ;; avoid cycle deps
   ))

(set! *warn-on-reflection* true)

(defn root-handler
  [{::system/keys [db]} _request]
  (let  [characters (query/select db query/characters)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body
     (try
       (concat
        (presentation/make-list "Characters List" characters)
        (presentation/make-table "Characters Table" characters)
        (presentation/show-res "Bar Chart" "/myplot.png")
        (presentation/show-res "Line Chart" "/myplot.png")
        (presentation/show-res "Chart" "/myplot.png")
        (presentation/make-form "post" "/refresh" "Refresh graphs"))
       (catch Exception e
         (concat "Error caught while generating html: "
                 (.getMessage e))))}))

(defn admin-handler
  [{::system/keys [db]} _request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (concat
    (for [table (query/bin db)]
      (presentation/make-table (concat "Table: " (:name table)) (:content table)))
	;;
    (map presentation/make-list
         ["Time" "Users" "Character Names" "DB Schemas" "DB"]
         (map (partial query/select db)
              [query/dbtime query/users query/names query/schemas query/database])))})

;; partial serves to provide system to handlers
;; f(system, request) -> response becomes f(request) -> response
(defn routes
  [system]
  [["/admin" {:get {:handler (partial #'admin-handler system)}}]
   ["/" {:get {:handler (partial #'root-handler system)}}]])
