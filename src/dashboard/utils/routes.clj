(ns dashboard.utils.routes
  (:require [dashboard.shared-html.core :as page-html]
            [dashboard.system :as-alias system] ;; avoid cycle deps
            [hiccup2.core :as hiccup]))

(defn utils-handler
  [_system _request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (str
    (hiccup/html
     (page-html/view :body [:h1 "Goodbye, world"])))})

;; partial serves to provide system to handlers
;; f(system, request) -> response becomes f(request) -> response
(defn routes
  [system]
  [["/goodbye" {:get {:handler (partial #'utils-handler system)}}]])
