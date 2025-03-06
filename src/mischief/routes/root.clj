(ns mischief.routes.root
  (:require
   [mischief.queries.sql :as query]
   [mischief.system :as-alias system] ;; avoid cycle deps
   ))

(defn root-handler
  [{::system/keys [db]} _request]
  (let  [characters (query/select db query/characters)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body
     (concat
      (query/hello)
      (query/to-html-list characters)
      (query/to-html-table characters)
	  ;; (query/to-html-graph numeric-values)
      )}))

(defn admin-handler
  [{::system/keys [db]} _request]
  (let
	[db-time (query/select db query/db-time)
     db-users (query/select db query/db-users)
     db-names (query/select db query/db-names)
     db-schemas (query/select db query/db-schemas)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body
     (concat
      (query/hello)
      (query/to-html-list "Time" db-time)
      (query/to-html-list "Users" db-users)
      (query/to-html-list "Character Names" db-names)
      (query/to-html-list "DB Schemas" db-schemas))}))

;; partial serves to provide system to handlers
;; f(system, request) -> response becomes f(request) -> response
(defn routes
  [system]
  [["/admin" {:get {:handler (partial #'admin-handler system)}}]
   ["/" {:get {:handler (partial #'root-handler system)}}]])
