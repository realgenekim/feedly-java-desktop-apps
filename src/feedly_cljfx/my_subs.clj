(ns feedly-cljfx.my-subs
  (:require
    [cljfx.api :as fx]
    [feedly.search :as search]))

(defn filtered-list
  [context]
  (let [stories (fx/sub-val context :stories)
        text    (fx/sub-val context :search-text)
        _       (println "subs: filtered-list: search: " text)
        fl      (if (or (nil? text)
                        (= "" text))
                  '()
                  (search/filtered-list stories text))]
    (println "subs: filtered-list: count: " (count fl))
    fl))


(defn active-stories
  " wrapper around search/active-stories "
  [context]
  (let [fl (fx/sub-ctx context filtered-list)
        sl (fx/sub-val context :stories)]
    (search/active-stories fl sl)))