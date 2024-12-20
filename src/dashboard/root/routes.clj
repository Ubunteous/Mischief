(ns dashboard.root.routes
  (:require
   [dashboard.queries.sql :as query]
   [dashboard.system :as-alias system] ;; avoid cycle deps
   ))

(defn root-handler
  [{::system/keys [db]} _request]
  (let [characters (query/select db query/characters)
        characters-ages (query/select db query/char-age)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body
     (concat
      (query/hello)
      (query/to-html-list characters)
      (query/to-html-table characters)
      (query/to-graph characters-ages))}))

;; partial serves to provide system to handlers
;; f(system, request) -> response becomes f(request) -> response
(defn routes
  [system]
  [["/" {:get {:handler (partial #'root-handler system)}}]])
