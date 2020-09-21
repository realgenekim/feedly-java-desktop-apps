(ns stories
  (:require
    [htmlcleaner]))

(defn text [s]
  (htmlcleaner/parse-page s))

