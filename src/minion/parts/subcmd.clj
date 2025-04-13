(ns minion.parts.subcmd
  (:require [babashka.fs :as fs]
            [minion.parts.db :as db]))

(defn create-table [m]
  (assoc m :fn :create-table))

(defn select [target]
  (let [table (get-in target [:opts :table])]
    (clojure.pprint/pprint (db/select-all db/args table))))

(defn update [m])

(defn help [m]
  ;; also try (cli/dispatch table args)
  (prn "Error with these args:" (assoc m :fn :help)))

;; (defn file-exists?
;;   [type file]
;;   (let [path (format "./%s/%s.%s" type file type)
;;         file-found? (fs/exists? path)]

;;     ;; replace long default error message
;;     (when-not file-found?
;;       (prn (format "Invalid path: %s in (called from %s)" path (System/getProperty "user.dir")))
;;       (System/exit 1))
;;     file-found?))
