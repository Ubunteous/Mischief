(ns dashboard.root.routes
  (:require
   [dashboard.shared-html.core :as page-html]
   [dashboard.system :as-alias system] ;; avoid cycle deps
   [hiccup2.core :as hiccup]
   [honey.sql :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(def sqlmap {:select [:name]
             :from   [:chars]})

(defn root-handler
  [{::system/keys [db]} _request]
  (let [{character :name}
        (jdbc/execute-one!
         db
         ;; ["select name from chars"]
         (sql/format sqlmap)
         {:builder-fn rs/as-unqualified-lower-maps})]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str
            (hiccup/html
             (page-html/view :body [:h1 (str "Hello, " character)])))}))

;; partial serves to provide system to handlers
;; f(system, request) -> response becomes f(request) -> response
(defn routes
  [system]
  [["/" {:get {:handler (partial #'root-handler system)}}]])
