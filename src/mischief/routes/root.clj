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
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (concat
    (query/hello)

    (map query/to-html-list
         ["Time" "Users" "Character Names" "DB Schemas" "DB"]
         (map (partial query/select db)
              [query/time query/users query/names query/schemas query/database])))})

;; partial serves to provide system to handlers
;; f(system, request) -> response becomes f(request) -> response
(defn routes
  [system]
  [["/admin" {:get {:handler (partial #'admin-handler system)}}]
   ["/" {:get {:handler (partial #'root-handler system)}}]])
