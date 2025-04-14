(ns minion.parts.subcmd
  (:require [babashka.fs :as fs]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.pprint]
            [clojure.string]
            [minion.parts.db :as db]))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ; header
            (map keyword) ; drop string keys
            repeat)
       (rest csv-data)))

(defn read-csv [csv-filename]
  (with-open [reader (io/reader (str db/res-path "csv/" csv-filename ".csv"))]
    (doall
     (csv-data->maps (csv/read-csv reader)))))

(defn process-csv-keys
  [csv-map]
  (let [rename-keys (fn [m f] (into {} (for [[k v] m] [(f k) v])))
        symbol-lower-case (fn [s] (keyword (clojure.string/lower-case (name s))))]
    (for [row csv-map]
      (rename-keys row symbol-lower-case))))

;; (defn create-table [m]
;;   (assoc m :fn :create-table))

(defn select
  [target]
  (let [table (get-in target [:opts :table])]
    (clojure.pprint/pprint (db/select-all table))))

(defn upsert-csv
  [target]
  (let [table-name (get-in target [:opts :table])
        csv-data (process-csv-keys
                  (read-csv table-name))
        foreign-cols (db/get-foreign-cols table-name)
        col-types (db/get-cols-types table-name)
        col-converter (db/get-cols-converter col-types)
        cols-typed (map (partial db/convert-columns col-converter) csv-data)
        processed (db/replace-foreign-keys cols-typed foreign-cols)]

    (println "\nCurrent State:")
    (clojure.pprint/print-table (db/select-all table-name))

    (db/upsert table-name processed)

    (println "\nTransaction Complete:")
    (clojure.pprint/print-table (db/select-all table-name))))

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
