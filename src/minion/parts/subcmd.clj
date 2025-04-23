(ns minion.parts.subcmd
  (:require [babashka.process :refer [shell pipeline pb process]]
            [clojure.pprint :as pprint]
            [clojure.string :as string]
            [minion.parts.db :as db]
            [minion.parts.file-io :as fio])
  (:import (java.time LocalDateTime)
           (java.time.format DateTimeFormatter)))

(set! *warn-on-reflection* true)

;;;;;;;;;;;;
;; SELECT ;;
;;;;;;;;;;;;

(defn use-function-on-keys [f m]
  (reduce (fn [acc [k v]] (assoc acc (f k) v))
          {} m))

(defn clean-pprint
  [m]
  (pprint/print-table
   (map	(partial use-function-on-keys
                 (fn [k] (keyword (name k))))
        m)))

(defn select
  [target]
  (let [table (get-in target [:opts :table])]
    (clean-pprint (db/select-all table))))

(defn get-tables
  [_target]
  (clean-pprint
   (db/select-tables)))

;;;;;;;;;;;;;;;;;;;
;; UPDATE/UPSERT ;;
;;;;;;;;;;;;;;;;;;;

(defn upsert-csv
  [target]
  (let [table-name (get-in target [:opts :table])
        csv-input (fio/read-csv table-name)
        col-type-converter
        (partial
         db/convert-col-types
         (db/get-cols-type-converter
          (db/get-cols-types table-name)))]

    (println "\nCurrent State:")
    (clean-pprint (db/select-all table-name))

    (db/upsert
     table-name
     (db/swap-foreign-with-ids
      (map col-type-converter (fio/process-csv-keys csv-input))
      table-name))

    (println "\nTransaction Complete:")
    (clean-pprint (db/select-all table-name))))

;;;;;;;;;;;;
;; BACKUP ;;
;;;;;;;;;;;;

(defn format-current-date []
  (.format
   (LocalDateTime/now)
   (DateTimeFormatter/ofPattern "dd-MM-yyyy")))

(defn backup
  [_target]
  (fio/create-dir-if-missing "./backups")
  (do (shell {:out (str "./backups/backup-" (format-current-date) ".sql")}
			 ;; "-p" (:port db/args)
             "pg_dump" "-U" (:user db/args) (:dbname db/args)))
  nil)

(defn restore
  [_target]
  (let [backup-dir "./backups/"
        backup-file (->
                     (pipeline
                      (pb "ls" "-1" backup-dir)
                      (pb "sort" "-V")
                      (pb "tail" "-n" "1"))
                     last :out slurp string/trim-newline)
        stream (-> (process "cat" (str backup-dir backup-file)) :out)]

    (if (empty? backup-file)
      (prn "No backup file found")
      (do
        (shell
         {:in stream}
         "psql" "-U" (:user db/args) "-X" (:dbname db/args))
        (print "\nRestoration successful")))))

;;;;;;;;;;
;; HELP ;;
;;;;;;;;;;

(defn help [m]
  ;; also try (cli/dispatch table args)
  (prn "Error with these args:" (assoc m :fn :help)))
