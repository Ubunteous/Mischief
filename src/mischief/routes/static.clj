(ns mischief.routes.static
  (:require [ring.util.response :as response]))

(defn favicon-ico-handler
  [& _]
  (response/resource-response "/favicon.ico"))

(defn plot-handler
  [& _]
  (response/resource-response "/myplot.png"))

(defn css-handler
  [& _]
  (response/resource-response "/stylesheet.css"))

(defn routes
  [_]
  [["/favicon.ico" favicon-ico-handler]
   ["/myplot.png" plot-handler]
   ["/stylesheet.css" css-handler]])
