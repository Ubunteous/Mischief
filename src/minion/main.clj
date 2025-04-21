#!/usr/bin/env bb

(ns minion.main
  (:require [babashka.cli :as cli]
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
   {:cmds [] :fn subcmd/help}])

(cli/dispatch table *command-line-args*)
