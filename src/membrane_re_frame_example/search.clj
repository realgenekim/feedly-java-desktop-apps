(ns membrane-re-frame-example.search
  (:require
    [defun.core :refer [defun]]))

(defn filtered-list
  " given list of stories, filter by presence of terms"
  [stories search]
  (if (= search "")
    stories
    (let [re (re-pattern (format "(?i)%s" search))]
      (->> stories
           (filter (fn [x]
                     (let [s (-> x :content :content)]
                       ;(println s)
                       (if s
                         (re-find re s)
                         false))))))))


(defun current-story
  " filtered-seq raw-seq story-num "
  ([fr :guard #(empty? %) rs story-num] (nth rs story-num))
  ([fr rs story-num] (nth fr story-num)))

(defun story-titles
  " filtered-seq raw-seq "
  ([fr :guard #(empty? %) rs]
   (map :title rs))
  ([fr rs]
   (map :title fr)))

(defun active-stories
  " filtered-seq raw-seq story-num "
  ([fr :guard #(empty? %) rs]
   rs)
  ([fr rs]
   fr))
