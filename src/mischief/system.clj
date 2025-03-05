(ns mischief.system
  (:require [mischief.routes :as routes]
            [next.jdbc.connection :as connection]
            [ring.adapter.jetty :as jetty])
  (:import (com.zaxxer.hikari HikariDataSource)
           (io.github.cdimascio.dotenv Dotenv)
           (org.eclipse.jetty.server Server)))

(set! *warn-on-reflection* true)

(defn start-env
  []
  (Dotenv/load))

(defn start-db
  [{::keys [env]}]
  (connection/->pool HikariDataSource
                     {:dbtype "postgres"
                      :dbname (Dotenv/.get env "POSTGRES_DBNAME")
                      :username (Dotenv/.get env "POSTGRES_USERNAME")
                      :password (Dotenv/.get env "POSTGRES_PASSWORD")}))

(defn stop-db
  [db]
  (HikariDataSource/.close db))

(defn start-server
  [{::keys [env] :as system}]
  (let [handler
        (if (= (Dotenv/.get env "ENVIRONMENT") "development")
          (partial #'routes/main-handler system)
          (routes/main-handler system))]
    (jetty/run-jetty
     handler
     {:port (Long/parseLong (Dotenv/.get env "PORT"))
      ;; start in background
      :join? false})))

(defn stop-server
  [server]
  (Server/.stop server))

(defn start-system
  []
  ;; ::server expands to :dashboard.system/server
  ;; refers to the system minus the server
  (let [system-so-far {::env (start-env)}
        system-so-far (merge system-so-far {::db (start-db system-so-far)})]
    (merge system-so-far {::server (start-server system-so-far)})))

(defn stop-system
  [system]
  (stop-server (::server system))
  (stop-db (::db system)))
