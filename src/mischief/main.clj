(ns mischief.main
  (:require [mischief.system :as system]))

(defn -main []
  (system/start-system))
