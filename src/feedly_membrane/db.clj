(ns feedly-membrane.db
  (:require
    [clojure.spec.alpha :as s]))

(s/def ::stories sequential?)
(s/def ::db (s/keys :req-un [::stories]))

;; (defn read-file []
;  (-> (slurp (str "/Users/genekim/src.local/feedly" "/" "ccsp2.txt"))
;      (read-string)))

(defn read-file []
  ; "ccsp2.txt"
  ; "clojure.txt"
  ; "/Users/genekim/src.local/feedly" "/" "clojure.txt"
  ; "resources/stories.txt"
  (-> (slurp
        ;(str "/Users/genekim/src.local/feedly" "/" "clojure.txt")
        "resources/stories.txt")
      read-string))
      ;(take 10)))

(def default-db
  {:text ""
   :story-num 0
   :stories (read-file)})

(defn pp-str [x]
  (with-out-str (clojure.pprint/pprint x)))

(comment
  (def text (-> (slurp (str "/Users/genekim/src.local/feedly" "/" "ccsp2.txt"))
                (read-string)))

  (def stories (:stories default-db))
  (doall (spit "resources/stories.txt"
               (pp-str (take 500 stories)))))


