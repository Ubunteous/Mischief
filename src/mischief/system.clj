(ns mischief.system
  (:require
   [mischief.routes :as routes]
   ;; [next.jdbc.connection :as connection]
   [pg.core :as pg]
   ;; [pg.pool :as pool]
   [ring.adapter.jetty :as jetty])
  (:import
   ;; (com.zaxxer.hikari HikariDataSource)
   (io.github.cdimascio.dotenv Dotenv)
   (org.eclipse.jetty.server Server)))

(set! *warn-on-reflection* true)

(defn start-env
  []
  ;; the .env file contains:
  ;; ENVIRONMENT=development
  ;; POSTGRES_DBNAME=<secret>
  ;; POSTGRES_USERNAME=<secret>
  ;; POSTGRES_PASSWORD=<secret>
  ;; PORT=<num>
  (Dotenv/load))

;;;;;;;;;;;;
;; CLJ DB ;;
;;;;;;;;;;;;

(defn start-db
  [{::keys [env]}]
  (pg/pool
   {:host (Dotenv/.get env "POSTGRES_HOST")
    :port (Integer. (Dotenv/.get env "POSTGRES_PORT"))
    :user (Dotenv/.get env "POSTGRES_USERNAME")
    :password (Dotenv/.get env "POSTGRES_PASSWORD")
    :database (Dotenv/.get env "POSTGRES_DBNAME")}))

(defn stop-db
  [db]
  (pg/close db))

(defn start-db-only
  []
  (start-db {::env (start-env)}))

;; ;;;;;;;;;;;;;
;; ;; JAVA DB ;;
;; ;;;;;;;;;;;;;

;; (defn start-db
;;   [{::keys [env]}]
;;   (connection/->pool HikariDataSource
;;                      {:dbtype "postgres"
;;                       :dbname (Dotenv/.get env "POSTGRES_DBNAME")
;;                       :username (Dotenv/.get env "POSTGRES_USERNAME")
;;                       :password (Dotenv/.get env "POSTGRES_PASSWORD")
;;                       ;; :connectionTestQuery false
;;                       }))

;; (defn stop-db
;;   [db]
;;   (HikariDataSource/.close db))

;;;;;;;;;;;;
;; SERVER ;;
;;;;;;;;;;;;

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

;;;;;;;;;;;;
;; SYSTEM ;;
;;;;;;;;;;;;

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
