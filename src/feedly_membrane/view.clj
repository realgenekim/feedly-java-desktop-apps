(ns feedly-membrane.view
  (:require
    [membrane.skia :as skia]
    [membrane.basic-components :as basic]
    [membrane.lanterna :as lanterna]
    [membrane.re-frame :as memframe]
    [membrane.ui :as ui :refer [horizontal-layout vertical-layout on]]
    [re-frame.core :as rf :refer [reg-event-db reg-event-fx inject-cofx path after reg-sub subscribe dispatch]]
    [feedly-membrane.text :as text]
    ; note that ::events/event-name is required to use aliased namespace
    [feedly-membrane.events :as events]
    [feedly-membrane.subs :as subs]
    [feedly-membrane.htmlcleaner :as html]))


(defn search-input [{:keys [id title on-save on-stop]}]
  (let [input-id [:search-input id]
        text     @(rf/subscribe [:searchbox-text])]
    (horizontal-layout
      (ui/button "Search"
                 (fn []
                   (println "search-input: ENTER:")
                   [(on-save text)
                    [:set-search-text input-id ""]]))
      (ui/wrap-on
        :key-press
        (fn [handler s]
          (println "search-input: keypress: " s)
          (if (= s :enter)
            [(on-save text)
             [:set-search-text input-id ""]]
            (handler s)))
        (ui/translate
          10 5
          (on
            :change
            (fn [s]
              [[:set-search-text input-id s]])
            (memframe/get-text-box input-id text)))))))

(defn search-entry
  []
  (search-input {:id "search"
                 :placeholder "search..."
                 :on-save #(do
                             [:search %])}))

(defn fix-scroll [elem]
  (ui/on-scroll (fn [[sx sy] mpos]
                  (ui/scroll elem [(- sx) (- sy)] mpos))
                elem))

(defn test-scrollview [text]
  [(ui/translate 10 10
                 (fix-scroll
                   (memframe/get-scrollview :my-scrollview [600 800]
                                            (ui/label text))))])

(defn get-story
  [s]
  (-> s :content :content))

(defn clean [x]
  (:content (html/parse-page x)))

(defn get-title
  [s]
  (:title s))

(defn gen-clickable-lines
  " input: seq of maps {:id xx, :title abc} "
  [articles]
  (apply vertical-layout
         (let [idxarticles (map-indexed (fn [idx itm]
                                          (merge itm {:idx idx}))
                                        articles)]
           (for [a idxarticles]
             (do
               ;(println "gen-clickable-lines: a: " a)
               (on
                 :mouse-down (fn [_]
                               ;; HELP: [[:select-article-id a]] didn't
                               (rf/dispatch [:select-article-id a]))
                 (ui/label (:title a))))))))

(defn stories
  []
  (let [curr-story       @(rf/subscribe [:current-story])
        text              (get-story curr-story)
        storynum         @(rf/subscribe [:story-num])
        stories          @(rf/subscribe [:active-stories])
        active-filter    @(rf/subscribe [:filter-text])
        filter-active?   (not (empty? active-filter))
        title            (format "%s: %s" storynum
                                 (-> curr-story :author)
                                 (-> curr-story :title))
        datestr          (str (java.util.Date. (:published curr-story)))
        plaintext        (clean text)]

    ;(println "filtered? " filter-active?)
    ;(println "# stories: " (count stories))

    (vertical-layout
      (ui/label (str "filtered? " filter-active?
                     (format " (\"%s\")" active-filter)))
      (ui/label (format "selected: %d of %d" storynum (count stories)))

      (ui/spacer 0 10)
      (ui/label (str "Article title: " title ": " (get-title curr-story)))
      (ui/label datestr)
      (ui/spacer 0 10)
      (horizontal-layout
        [(fix-scroll
           (memframe/get-scrollview
             :scrollview-list
             [300 800]
             (gen-clickable-lines stories)))]
        ;(ui/label (clojure.string/join "\n" titles))))]
        [(vertical-layout
           (test-scrollview (text/line-wrap plaintext 80)))]))))



(comment
  (re-frame.subs/clear-subscription-cache!))

(defn todo-app
  []
  (ui/translate
    10 10
    (vertical-layout
      (search-entry)
      [(stories)])))

(defn -main [& args]
  (println "main starting")
  (dispatch [:initialize-db])
  (println "hello")
  (skia/run
    #(memframe/re-frame-app (#'todo-app))))

(comment
  (defn -main [& args]
    (dispatch [:initialize-db])
    (lanterna/run #(memframe/re-frame-app
                       (fn []
                         (on :key-press
                                (fn [s]
                                  (println "main: keypress event: " s)
                                  [[:keydown s]])
                                (ui/label "Hello from key")))))))