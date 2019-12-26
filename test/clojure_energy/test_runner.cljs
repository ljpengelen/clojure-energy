(ns clojure-energy.test-runner
  (:require
    [clojure-energy.core-test]
    [doo.runner :refer-macros [doo-tests]]))

(doo-tests 'clojure-energy.core-test)
