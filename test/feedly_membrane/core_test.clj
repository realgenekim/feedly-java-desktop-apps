(ns membrane-re-frame-example.core-test
  (:require [clojure.test :refer :all]
            [membrane-re-frame-example.core :refer :all]
            [membrane-re-frame-example.search :as search]))

(def stories
  [{:content {:content "test1"}
    :title "title1"
    :id "1"}
   {:content {:content "test2"}
    :title "title2"
    :id "2"}])

(def stories2
  [{:content {:content "test"}
    :title "title3"
    :id "3"}
   {:content {:content "test2"}
    :title "title4"
    :id "4"}])

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(deftest filtered-list
  (is (= 2
         (search/current-story '() [1 2 3] 1)))
  (is (= 5
         (search/current-story [4 5 6] [1 2 3] 1)))

  (is (= ["title1" "title2"]
         (search/story-titles '() stories)))
  (is (= ["title3" "title4"]
         (search/story-titles stories2 stories)))

  (is (= stories2
         (search/active-stories '() stories2)))
  (is (= stories
         (search/active-stories stories stories2)))

  (is (= (list (first stories))
         (search/filtered-list stories "test1"))))



