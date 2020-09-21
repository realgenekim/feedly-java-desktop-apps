(ns feedly-membrane.subs
  (:require
    [re-frame.core :refer [reg-sub subscribe]]
    [feedly.search :as search]))


(comment
  (re-frame.subs/clear-subscription-cache!))

(reg-sub
  :searchbox-text
  (fn [db _]
    ;(println "sub: search-text")
    (:searchbox-text db)))

(reg-sub
  :search-text
  (fn [db [_ id]]
    (get db id "")))

(reg-sub
  :filter-text
  (fn [db _]
    ;(println "sub: search-text")
    (:filter-text db)))

(reg-sub
  :story-num
  (fn [db [_]]
    (:story-num db)))

(reg-sub
  :stories
  (fn [db [_]]
    (:stories db)))

(defn filter-active?
  " returns true: function of (:input-text db) "
  [input]
  (let [filter-active (case input
                        ; false if nil or blank
                        nil false
                        "" false
                        true)]
    ;(println "filter-active?: input: " input)
    filter-active))

(reg-sub
  :filtered-stories
  (fn [query-v _]
    [(subscribe [:filter-text])
     (subscribe [:stories])])
  (fn [[input stories] _]
    (let [f? (filter-active? input)]
      ;(println "sub: filtered-stories: " f? input (count stories))
      (let [filtered-list (if (not f?)
                            '()
                            (search/filtered-list stories input))]
        ;(println "sub: filtered-stories: count filtered: " (count filtered-list))
        filtered-list))))

(reg-sub
  :active-stories
  (fn [query-v _]
    [(subscribe [:filtered-stories])
     (subscribe [:stories])])
  (fn [[filtered raw] _]
    ;(println "sub: active-stories")
    (search/active-stories filtered raw)))

(reg-sub
  :story-titles
  (fn [query-v _]
    [(subscribe [:filtered-stories])
     (subscribe [:stories])])
  (fn [[fs rs] _]
    (search/story-titles fs rs)))

(reg-sub
  :current-story
  (fn [query-v _]
    [(subscribe [:filtered-stories])
     (subscribe [:stories])
     (subscribe [:story-num])])
  (fn [[filtered raw story-num] _]
    ;(println "sub: :current-story: " (count filtered) (count raw) story-num)
    (search/current-story filtered raw story-num)))





