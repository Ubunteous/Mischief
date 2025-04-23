#!/usr/bin/env bb

(ns minion.main
  (:require [babashka.cli :as cli]
            [clojure.string :as string]
            [minion.parts.subcmd :as subcmd]))

(def table
  [;; {:cmds ["create" "table"]
   ;;  :fn subcmd/create-table
   ;;  :args->opts [:target]
   ;;  :spec {:target {:validate (and (partial subcmd/file-exists? "csv")
   ;;                                 partial subcmd/file-exists? "edn")}}}
   {:cmds ["select"] :fn subcmd/select :args->opts [:table]}
   {:cmds ["upsert"] :fn subcmd/upsert-csv :args->opts [:table]}
   {:cmds ["update"] :fn subcmd/upsert-csv :args->opts [:table]}
   {:cmds ["tables"] :fn subcmd/get-tables}
   {:cmds ["backup"] :fn subcmd/backup}
   {:cmds ["restore"] :fn subcmd/restore}
   {:cmds [] :fn subcmd/help}])

(let [args *command-line-args*
      opts (cli/parse-opts args table)]
  (if (or (contains? opts :h)
          (contains? opts :help))
    (println "Commands:"
             (string/join ", " (butlast (flatten (map :cmds table)))))
    (cli/dispatch table args)))
