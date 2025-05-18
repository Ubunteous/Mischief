(ns minion.parts.file-io
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string]))

(set! *warn-on-reflection* true)

;;;;;;;;;;
;; .env ;;
;;;;;;;;;;

(def env (into {}
               (map (fn [pair]
                      (let [[k v] (clojure.string/split pair #"=")]
                        [(keyword k) v]))
                    (clojure.string/split (slurp ".env") #"\n"))))

(def res-path (:RES_PATH env))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->>
        (first csv-data)
        (map keyword)
        repeat)
       (rest csv-data)))

;;;;;;;;;;;
;; files ;;
;;;;;;;;;;;

(defn create-dir-if-missing
  [path]
  (when-not (.exists (io/file path))
    (.mkdir (io/file path))))

(defn check-file-exists
  [path]
  ;; replace long default error message
  (when-not (.exists (io/file path))
    (println (format "Invalid path: %s\nCalled from: %s" path (System/getProperty "user.dir")))
    (System/exit 1))
  (io/reader path))

;;;;;;;;;
;; csv ;;
;;;;;;;;;

(defn read-csv [csv-filename]
  (let [filepath (str res-path "csv/" csv-filename ".csv")]
    (check-file-exists filepath)
    (with-open [reader (check-file-exists filepath)]
      (doall
       (csv-data->maps (csv/read-csv reader :separator \;))))))

(defn process-keys
  [csv-map]
  (let [rename-keys (fn [m f] (into {} (for [[k v] m] [(f k) v])))
        symbol-lower-case (fn [s] (keyword (clojure.string/lower-case (name s))))]
    (for [row csv-map]
      (rename-keys row symbol-lower-case))))
