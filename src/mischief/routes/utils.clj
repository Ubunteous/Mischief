(ns mischief.routes.utils
  (:require
   [mischief.presentation.graphs :as graphs]
   [mischief.queries.sql :as query]
   [mischief.system :as-alias system] ;; avoid cycle deps
   [ring.util.response :as response]))

(defn utils-handler
  ;; [_system _request]
  [{::system/keys [db]} _request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   ;; (str
   ;; (hiccup/html
   ;; (presentation/view :body [:h1 "Goodbye, world"])))
   "a"})

(defn refresh-graphs-handler
  [{::system/keys [db]} _request]
  (let [char-ages (query/select db query/ages)
        ;; age-wealth (query/select db query/age-wealth)
        ]
    (graphs/save-bar-chart char-ages)
    ;; (graphs/save-line-chart age-wealth)
    (response/redirect "/")))

;; partial serves to provide system to handlers
;; f(system, request) -> response becomes f(request) -> response
(defn routes
  [system]
  [["/goodbye" {:get {:handler (partial #'utils-handler system)}}]
   ["/refresh" {:get {:handler (partial #'refresh-graphs-handler system)}
                :post {:handler (partial #'refresh-graphs-handler system)}}]])
