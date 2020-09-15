(ns feedly-membrane.text)

(defn line-wrap
  " input: s: string
           n: number of characters "
  [s n]
  (loop [s (seq s)
         current-line []
         lines []
         i 0]
    (if s
      (let [c (first s)]
        (cond
          (= c \newline)
          (do
            (recur (next s)
                   ; [] for not inserting extra newline
                   [\newline]
                   (conj lines current-line)
                   0))
          (>= i n)
          (recur s
                 []
                 (conj lines current-line)
                 0)
          :else
          (recur (next s)
                 (conj current-line c)
                 lines
                 (inc i))))

      (clojure.string/join "\n" (map (partial apply str) lines)))))
;(def line-wrap-memo (memoize line-wrap))
;(defn test-scrollview []
;  [(ui/translate 10 10
;                 (fix-scroll
;                   (get-scrollview :my-scrollview [300 300]
;                                   (ui/label (line-wrap-memo lorem-ipsum 20)))))])