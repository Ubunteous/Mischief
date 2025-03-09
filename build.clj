(ns build
  (:require [clojure.tools.build.api :as b]))

;; (def lib 'mischief/main)
(def lib 'com.github.ubunteous/mischief)
(def version (format "1.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def copy-srcs ["src" "res"])
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

;; delay to defer side effects (artifact downloads)
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis @basis
                :src-dirs ["src"]})
  (b/copy-dir {:src-dirs copy-srcs
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn uber [_]
  (clean nil)
  ;; not ready yet
  ;; (b/copy-file {:src ".env" :target "target/classes/.env"}) ;; not ready
  (b/copy-dir {:src-dirs copy-srcs
               :target-dir class-dir})
  (b/compile-clj {:basis @basis
                  :ns-compile '[mischief.main]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis @basis
           :main 'mischief.main}))
