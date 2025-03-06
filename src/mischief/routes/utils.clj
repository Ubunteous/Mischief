(ns mischief.routes.utils
  (:require [hiccup2.core :as hiccup]
            [mischief.html.core :as page-html]
            [mischief.system :as-alias system] ;; avoid cycle deps
            ))

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
