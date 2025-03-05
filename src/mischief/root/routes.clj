(ns mischief.root.routes
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

(defn root-admin-handler
  [{::system/keys [db]} _request]
  (let
   [db-users (query/select db query/db-users)
    db-names (query/select db query/db-names)
    db-schemas (query/select db query/db-schemas)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body
     (concat
      (query/hello)
      (query/to-html-list db-users)
      (query/to-html-list db-names)
      (query/to-html-list db-schemas))}))

;; partial serves to provide system to handlers
;; f(system, request) -> response becomes f(request) -> response
(defn routes
  [system]
  [["/admin" {:get {:handler (partial #'root-admin-handler system)}}]
   ["/" {:get {:handler (partial #'root-handler system)}}]])
