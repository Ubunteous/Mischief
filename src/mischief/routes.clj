(ns mischief.routes
  (:require [clojure.tools.logging :as log]
            ;; [clojure.pprint :as pp]
            ;; [clojure.string :as string]
            [hiccup2.core :as hiccup]
            ;; routes
            [mischief.routes.root :as root-routes]
            [mischief.routes.static :as static-routes]
            [mischief.routes.utils :as utils-routes]
            [mischief.system :as-alias system] ;; avoid cycle deps
            [reitit.ring :as reitit-ring]))

(defn routes
  [system]
  [""
   (root-routes/routes system)
   (static-routes/routes system)
   (utils-routes/routes system)])

(defn log-access
  [request]
  (log/info (str
             "\nSalut mon loulou! Ça toque au "
             (:uri request)
             " avec du "
             (:request-method request)
             "\n")))

(defn not-found-handler
  [_request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body
   (str
    (hiccup/html
     [:html
      [:body
       [:h1 "Error 404: Page Not Found"]]]))})

;; use currying to either compile per-request
;; or just provide the system to pre-compile
(defn main-handler
  ([system request]
   ((main-handler system) request))
  ([system]
   (let [handler (reitit-ring/ring-handler
                  (reitit-ring/router
                   (routes system))
                  #'not-found-handler)]
     ;; anonymous function named for explicit stacktraces
     (fn main-handler [request]
       (log-access request)
       (handler request)))))
