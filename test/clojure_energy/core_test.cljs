(ns clojure-energy.core-test
  (:require [clojure-energy.core :as c]
            [cljs.core.async :as a :refer [<! >! chan go put!]]
            [cljs.test :refer-macros [deftest testing is async]]))

(deftest test-down
  (testing "Moves item down in collection of sorted words"
    (reset! c/sorted-words [1 2 3])
    (c/down 1)
    (is @c/sorted-words [1 3 2])))


(deftest test-up
  (testing "Moves item up in collection of sorted words"
    (reset! c/sorted-words [1 2 3])
    (c/up 1)
    (is @c/sorted-words [2 1 3])))

(deftest test-sort
  (testing "Sorts items asynchronously"
    (async done
      (let [opts-c (chan)
            prefs-c (chan)
            res-c (c/async-sort [9 8 7 6 5 4 3 2 1] opts-c prefs-c)]
        (go
          (while true
            (let [[a b] (<! opts-c)]
              (if (< a b)
                (put! prefs-c true)
                (put! prefs-c false)))))
        (go
          (let [res (<! res-c)]
            (is (= res [1 2 3 4 5 6 7 8 9]))
            (done)))))))
