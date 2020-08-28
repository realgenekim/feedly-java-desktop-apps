(ns membrane-re-frame-example.htmlcleaner
;(:require)
;[clojure.contrib.logging :as log])
  (:import
    [org.htmlcleaner HtmlCleaner]
    [org.apache.commons.lang StringEscapeUtils]))


(defn parse-page
  "Given the HTML source of a web page, parses it and returns the :title
   and the tag-stripped :content of the page."
  [page-src]
  (try
    (when page-src
      (let [cleaner (new HtmlCleaner)]
        (doto (.getProperties cleaner) ;; set HtmlCleaner properties
          (.setOmitComments true)
          (.setPruneTags "script,style"))
        (when-let [node (.clean cleaner page-src)]
          {:title   (when-let [title (.findElementByName node "title", true)]
                      (-> title
                          (.getText)
                          (str)
                          (StringEscapeUtils/unescapeHtml)))
           :content (-> node
                        (.getText)
                        (str)
                        (StringEscapeUtils/unescapeHtml))})))
    (catch Exception e
      (println "Error when parsing" e))))


(comment
  (def s (slurp "https://porkostomus.gitlab.io/posts-output/2018-08-29-Just-Juxt-15/"))
  (def s (slurp "http://sids.in/blog/2010/05/html-parsing-in-clojure-using-htmlcleaner/"))
  (parse-page s))