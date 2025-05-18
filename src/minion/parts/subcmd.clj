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
          (db/get-cols-types table-name)))

        converted-cols
        (map col-type-converter (fio/process-keys csv-input))]

    (println "\nCurrent State:")
    (clean-pprint (db/select-all table-name))

    (db/minimise-table-iterator table-name)

    (db/upsert
     table-name
     (db/swap-foreign-with-ids
      converted-cols
      table-name))

    (println "\nTransaction Complete:")
    (clean-pprint (db/select-all table-name))))

;;;;;;;;;;;
;; CROSS ;;
;;;;;;;;;;;

(defn get-cross-prefix [m]
  (filter #(string/starts-with? (name %) "X")
          (keys (if (list? m) (first m) m))))

(defn remove-cross [key]
  (keyword (string/replace-first (str key) ":X" "")))

(defn cross-split [key table-name data]
  (let [elements (clojure.string/split (key data) #", ")]
    (map #(assoc {(keyword table-name) (:Name data)} (remove-cross key) %) elements)))

(defn cross-split-map [key table-name data]
  (flatten (map (partial cross-split key table-name) data)))

(defn upsert-cross-csv
  [target]
  (let [original-table-name (get-in target [:opts :table])
        csv-input (fio/read-csv original-table-name)]

    (doseq [cross-prefix (get-cross-prefix (first csv-input))]
      (let [table-name (str original-table-name (string/lower-case (name cross-prefix)))

            col-type-converter
            (partial
             db/convert-col-types
             (db/get-cols-type-converter
              (db/get-cols-types table-name)))

            converted-cols
            (map col-type-converter
                 (fio/process-keys
                  (cross-split-map cross-prefix original-table-name csv-input)))]

        ;; (println "\nCurrent State:")
        ;; (clean-pprint (db/select-all table-name))

        ;; (db/minimise-table-iterator table-name)

        (db/cross-upsert
         table-name
         (db/swap-foreign-with-ids
          converted-cols
          table-name))

        ;; (println "\nCurrent State:")
        ;; (clean-pprint (db/select-all table-name))
        ))))

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
  (println "Error with these args:" (assoc m :fn :help)))
