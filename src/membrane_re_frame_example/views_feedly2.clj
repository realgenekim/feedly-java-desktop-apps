(ns membrane-re-frame-example.views-feedly2
  (:require
    [membrane.skia :as skia]
    [membrane.basic-components :as basic]
    [membrane.lanterna :as lanterna]
    [membrane.re-frame :as memframe]
    [membrane-re-frame-example.events :as events]
    [membrane.ui :as ui
     :refer
     [horizontal-layout
      vertical-layout
      on]]
    [re-frame.core :as rf :refer [reg-event-db reg-event-fx inject-cofx path after reg-sub subscribe dispatch]]

    membrane-re-frame-example.db
    membrane-re-frame-example.subs
    membrane-re-frame-example.events
    [membrane-re-frame-example.text :as text]
    [membrane-re-frame-example.htmlcleaner :as html]
    [membrane-re-frame-example.search :as search]))





(defn search-input [{:keys [id title on-save on-stop]}]
  (let [input-id [:search-input id]
        text     @(rf/subscribe [:searchbox-text input-id])]
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
        filter-active?   @(rf/subscribe [:filter-active?])
        storynum         @(rf/subscribe [:story-num])
        stories          @(rf/subscribe [:active-stories])
        active-filter    @(rf/subscribe [:filter-text])
        title            (format "%s: %s" storynum
                                 (-> curr-story :author)
                                 (-> curr-story :title))
        datestr          (str (java.util.Date. (:published curr-story)))
        plaintext        (clean text)]

    ;(println "filtered? " filter-active?)
    ;(println "# stories: " (count stories))

    (vertical-layout
      (ui/label (str "filtered? " filter-active?))
      (ui/label (format "selected: %d of %d" storynum (count stories)))
      (ui/label (str "Title: " (get-title curr-story)))
      (ui/label (format "    (search: \"%s\")" active-filter))
      (ui/spacer 0 10)
      (ui/label title)
      (ui/label datestr)
      (ui/spacer 0 10)
      ;(ui/label (format "%d of %d" storynum (count stories)))
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
      ;(task-entry)
      ;(ui/spacer 0 20)
      ;(when (seq @(subscribe [:todos]))
      ;    (task-list))
      ;(ui/spacer 0 20)
      ;(footer-controls))))




(println "global hello")

(defn -main [& args]
  (println "main starting")
  (dispatch [:initialize-db])
  (println "hello")
  (skia/run
    #(memframe/re-frame-app (#'todo-app))))

                       ;(ui/label "hello")))))
  ;(skia/run #(memframe/re-frame-app (todo-app))))
;#_(ui/mouse-down
 ;(ui/translate 50 50
 ;              (on
 ;               :mouse-down
 ;               (fn [[x y]]
 ;                 (prn x y))
 ;               (ui/button "asdfasdfasfdaf")))
 ;[51 51])

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