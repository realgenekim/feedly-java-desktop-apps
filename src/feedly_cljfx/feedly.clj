(ns feedly-cljfx.feedly)

(defn read-file []
  ; "ccsp2.txt"
  ; "clojure.txt"
  (-> ;(slurp (str "/Users/genekim/src.local/feedly" "/" "clojure.txt"))
      (slurp "resources/stories.txt")
      (read-string)))
