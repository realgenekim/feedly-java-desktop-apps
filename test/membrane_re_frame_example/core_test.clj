(ns membrane-re-frame-example.core-test
  (:require [clojure.test :refer :all]
            [membrane-re-frame-example.core :refer :all]
            [membrane-re-frame-example.search :as search]))

(def stories
  [{:content {:content "test"}
    :title "title1"
    :id "1"}
   {:content {:content "test2"}
    :title "title2"
    :id "2"}])

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(deftest filtered-list
  (is (= 2
         (search/current-story '() [1 2 3] 1)))
  (is (= 5
         (search/current-story [4 5 6] [1 2 3] 1))))

