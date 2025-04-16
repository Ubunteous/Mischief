(ns minion.parts.subcmd
  (:require [clojure.pprint]
            [minion.parts.db :as db]
            [minion.parts.file-io :as fio]))

(defn function-on-keys [f m]
  (reduce (fn [acc [k v]] (assoc acc (f k) v))
          {} m))

(defn clean-pprint
  [m]
  (clojure.pprint/print-table
   (map	(partial function-on-keys
                 (fn [k] (keyword (name k))))
        m)))

(defn select
  [target]
  (let [table (get-in target [:opts :table])]
    (clean-pprint (db/select-all table))))

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

(defn help [m]
  ;; also try (cli/dispatch table args)
  (prn "Error with these args:" (assoc m :fn :help)))
