(ns dashboard.main
  (:require [dashboard.system :as system]))

(defn -main []
  (system/start-system))
