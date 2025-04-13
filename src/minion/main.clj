#!/usr/bin/env bb

(ns minion.main
  (:require [babashka.cli :as cli]
            [minion.parts.subcmd :as subcmd]))

(def table
  [{:cmds ["create"] :fn subcmd/create-table :args->opts [:table]}
   ;; {:cmds ["create" "table"]
   ;;  :fn subcmd/create-table
   ;;  :args->opts [:target]
   ;;  :spec {:target {:validate (and (partial subcmd/file-exists? "csv")
   ;;                                 partial subcmd/file-exists? "edn")}}}
   {:cmds ["select"] :fn subcmd/select :args->opts [:table]}
   {:cmds ["update"] :fn subcmd/update :args->opts [:table]}
   {:cmds [] :fn subcmd/help}])

(cli/dispatch table *command-line-args*)
