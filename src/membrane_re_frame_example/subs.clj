(ns membrane-re-frame-example.subs
  (:require
    [membrane-re-frame-example.search :as search]
    [re-frame.core :refer [reg-sub subscribe]]))


(comment
  (re-frame.subs/clear-subscription-cache!))

(reg-sub
  :searchbox-text
  (fn [db _]
    ;(println "sub: search-text")
    (:searchbox-text db)))

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





;; -------------------------------------------------------------------------------------
;; Hey, wait on!!
;;
;; How did those two simple Layer 2 registrations at the top work?
;; We only supplied one function in those registrations, not two?
;; Very observant of you, I'm glad you asked.
;; When the signal-returning-fn is omitted, reg-sub provides a default,
;; and it looks like this:
;;    (fn [_ _]
;;       re-frame.db/app-db)
;; It returns one signal, and that signal is app-db itself.
;;
;; So the two simple registrations at the top didn't need to provide a signal-fn,
;; because they operated only on the value in app-db, supplied as 'db' in the 1st argument.
;;
;; So that, by the way, is why Layer 2 subscriptions always re-calculate when `app-db`
;; changes - `app-db` is literally their input signal.

;; -------------------------------------------------------------------------------------
;; SUGAR ?
;; Now for some syntactic sugar...
;; The purpose of the sugar is to remove boilerplate noise. To distill to the essential
;; in 90% of cases.
;; Because it is so common to nominate 1 or more input signals,
;; reg-sub provides some macro sugar so you can nominate a very minimal
;; vector of input signals. The 1st function is not needed.
;; Here is the example above rewritten using the sugar.
#_(reg-sub
   :visible-todos
   :<- [:todos]
   :<- [:showing]
   (fn [[todos showing] _]
     (let [filter-fn (case showing
                       :active (complement :done)
                       :done   :done
                       :all    identity)]
       (filter filter-fn todos))))


(reg-sub
  :all-complete?
  :<- [:todos]
  (fn [todos _]
    (every? :done todos)))

(reg-sub
  :completed-count
  :<- [:todos]
  (fn [todos _]
    (count (filter :done todos))))

(reg-sub
  :footer-counts
  :<- [:todos]
  :<- [:completed-count]
  (fn [[todos completed] _]
    [(- (count todos) completed) completed]))



