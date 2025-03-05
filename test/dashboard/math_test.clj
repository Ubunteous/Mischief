(ns mischief.math-test
  (:require [clojure.test :as test]))

(test/deftest one-plus-one
  (test/is (= (+ 1 1) 2) "One plus one equals 2"))
