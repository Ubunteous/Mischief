(ns mischief.routes.root
  (:require
   [mischief.presentation.html :as presentation]
   [mischief.queries.sql :as query]
   [mischief.system :as-alias system] ;; avoid cycle deps
   ))

(defn root-handler
  [{::system/keys [db]} _request]
  (let  [characters (query/select db query/characters)
         char-ages (query/select db query/ages)
         age-wealth (query/select db query/age-wealth)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body
     (concat
      ;; (presentation/hello)
      (presentation/make-list "Characters List" characters)
      (presentation/make-table "Characters Table" characters)
      ;; (presentation/make-bar-chart char-ages)
      ;; (presentation/make-line-chart age-wealth)
      )}))

(defn admin-handler
  [{::system/keys [db]} _request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (concat
    ;; (presentation/hello)
	;;
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
