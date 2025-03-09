(ns mischief.main
  (:require [mischief.system :as system])
  (:gen-class))

(defn -main [& args]
  (system/start-system))
