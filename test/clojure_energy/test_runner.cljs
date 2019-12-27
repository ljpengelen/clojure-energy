(ns clojure-energy.test-runner
  (:require
    [clojure-energy.core-test]
    [figwheel.main.testing :refer [run-tests-async]]))

(defn -main [& args]
  (run-tests-async 5000))
