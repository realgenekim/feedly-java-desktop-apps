(ns feedly-membrane.events
  (:require
    [clojure.spec.alpha :as s]
    [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path after]]
    [feedly-membrane.db :refer [default-db]]))



(defn check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db & args]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

;; now we create an interceptor using `after`
(def check-spec-interceptor
  (after (partial check-and-throw :feedly-membrane.db/db)))
(reg-event-fx                 ;; part of the re-frame API
  :initialize-db              ;; event id being handled

  ;; the interceptor chain (a vector of 2 interceptors in this case)
  [;; (inject-cofx :local-store-todos) ;; gets todos from localstore, and puts value into coeffects arg
   check-spec-interceptor]          ;; after event handler runs, check app-db for correctness. Does it still match Spec?

  ;; the event handler (function) being registered
  (fn [{:keys [db local-store-todos]} _]                  ;; take 2 values from coeffects. Ignore event vector itself.
    {:db default-db}))   ;; all hail the new state to be put in app-db


(reg-event-db
  :keydown
  [check-spec-interceptor]
  (fn [db [_ k]]     ;; new-filter-kw is one of :all, :active or :done
      (println "event: keydown: " k)
      (println "event: keydown: type: " (type k))
      (let [story-num     (:story-num db)
            new-story-num (case k
                            "j" (max 0 (inc story-num))
                            "k" (max 0 (dec story-num))
                            story-num)]

      ;(let [t (str (:text db) k)]
      ;  (println "t: " t)
        (assoc db :story-num new-story-num))))


(reg-event-db
  :set-search-text
  (fn [db [_ id s]]
    (println ":set-search-text: " id s)
    (assoc db id s
              :searchbox-text s)))

(reg-event-db
  :search
  (fn [db [_ id]]
    (println ":search: " (:searchbox-text db))
    (assoc db :filter-text (:searchbox-text db)
              :searchbox-text "")))

(reg-event-db
  :select-article-id
  [check-spec-interceptor]
  (fn [db [_ newv]]
    (let [id (:id newv)
          idx (:idx newv)]
      (println ":select-article-id: args: " idx id)
      (assoc db :story-num idx))))

