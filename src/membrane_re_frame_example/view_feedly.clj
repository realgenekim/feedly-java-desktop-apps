(ns membrane-re-frame-example.view-feedly
  (:require [membrane.skia :as skia]
            [membrane.basic-components :as basic]
            [membrane.lanterna :as lanterna]
            [membrane.re-frame :as memframe]
            [membrane-re-frame-example.events :as events]
            [membrane-re-frame-example.text :as text]
            [membrane.ui :as ui
             :refer
             [horizontal-layout
              vertical-layout
              on]]
            [re-frame.core :as rf :refer [reg-event-db reg-event-fx inject-cofx path after reg-sub subscribe dispatch]]
            membrane-re-frame-example.db
            membrane-re-frame-example.subs
            membrane-re-frame-example.events
            [membrane-re-frame-example.htmlcleaner :as html]))





(defn todo-input [{:keys [id title on-save on-stop]}]
  (let [input-id [:todo-input id]
        text @(rf/subscribe [:input-text input-id])]
    (horizontal-layout
     (ui/button "Add Todo 4"
                (fn []
                  [(on-save text)
                   [:set-input-text input-id ""]]))
     (ui/wrap-on
      :key-press
      (fn [handler s]
        (let [effects (handler s)]
          (println effects)
          (if (= s :enter)
            [(on-save text)
             [:set-input-text input-id ""]]
            (handler s))))
      (ui/translate
       10 5
       (on
        :change
        (fn [s]
          [[:set-input-text input-id s]])
        (memframe/get-text-box input-id text)))))))


(defn delete-X []
  (ui/with-style :membrane.ui/style-stroke
    (ui/with-color
      [1 0 0]
      (ui/with-stroke-width
        3
        [(ui/path [0 0]
                  [10 10])
         (ui/path [10 0]
                  [0 10])]))))

(defn todo-item
  [{:keys [id done title] :as todo}]
  (let [;;editing @(rf/subscribe [:extra [:editing (:id todo)]])
        input-id [:todo-input id]]

    (horizontal-layout
     (ui/translate 0 5
      (on
       :mouse-down
       (fn [_]
         [[:delete-todo id]])
       (delete-X)))
     (ui/spacer 5 0)
     (ui/translate 0 4
                   (on
                    :mouse-down
                    (fn [_]
                      [[:toggle-done id]])
                    (ui/checkbox done)))
     (ui/spacer 5 0)

     (on
      :change
      (fn [s]
        [[:save id s]])
      (memframe/get-text-box input-id title)))))


(defn task-list
  []
  (let [visible-todos @(subscribe [:visible-todos])
        all-complete? @(subscribe [:all-complete?])]
    (apply
     vertical-layout
     (interpose
      (ui/spacer 0 10)
      (for [todo visible-todos]
        (todo-item todo))))))


(defn footer-controls
  []
  (let [[active done] @(subscribe [:footer-counts])
        showing       @(subscribe [:showing])
        a-fn          (fn [filter-kw txt]
                        (on
                         :mouse-down
                         (fn [_]
                           [[:set-showing filter-kw]])
                         (ui/with-color (if (= filter-kw showing)
                                          [0 0 0]
                                          [0.7 0.7 0.7])
                           (ui/label txt))))]
    (vertical-layout
     (ui/label (str active
                    " "
                    (if (= 1 active)
                      "item"
                      "items")
                    " left"))
     (ui/spacer 0 5)
     (apply
      horizontal-layout
      (interpose
       (ui/spacer 5 0)
       (for [[kw txt] [[:all "All"]
                       [:active "Active"]
                       [:done "Completed"]]]
         (a-fn kw txt))))
     (ui/spacer 0 5)
     (when (pos? done)
       (ui/button "Clear completed"
                  (fn []
                    [[:clear-completed]]))))))


(defn task-entry
  []
  (todo-input
   {:id "new-todo"
    :placeholder "What needs to be done?"
    :on-save #(when (seq %)
                [:add-todo %])}))

(defn fix-scroll [elem]
  (ui/on-scroll (fn [[sx sy]]
                  (ui/scroll elem [(- sx) (- sy)]))
                elem))

(defn test-scrollview [text]
  [(ui/translate 10 10
                 (fix-scroll
                   (memframe/get-scrollview :my-scrollview [600 800]
                                            (ui/label text))))])

(def lorem-ipsum
  (clojure.string/join
    "\n"
    (repeatedly
      800
      (fn []
        (clojure.string/join
          (repeatedly (rand-int 1000)
                      #(rand-nth "abcdefghijklmnopqrstuvwxyz ")))))))

(defn clean [x]
  (:content (html/parse-page x)))

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

(def gen-clickable-lines-memo (memoize gen-clickable-lines))

(defn stories
  []
  (let [sts       @(rf/subscribe [:story-text])
        storynum  @(rf/subscribe [:story-num])
        titles    @(rf/subscribe [:story-titles])
        stories   @(rf/subscribe [:stories])
        title     (format "%d: %s: %s" storynum
                          (-> sts :author)
                          (-> sts :title))
        datestr   (str (java.util.Date. (:published sts)))
        text      (-> sts
                      :content
                      :content)
        plaintext (clean text)]

    (vertical-layout
      (ui/label "test")
      ;(ui/label (format "%d of %d" storynum (count stories)))
      (horizontal-layout
        [(fix-scroll
           (memframe/get-scrollview
             :scrollview-list
             [300 800]
             (gen-clickable-lines-memo stories)))]
             ;(ui/label (clojure.string/join "\n" titles))))]
        [(vertical-layout
           (ui/label title)
           (ui/label datestr)
           (test-scrollview (text/line-wrap plaintext 80)))]))))
    ;(test-scrollview lorem-ipsum)))
    ;(basic/test-scrollview)))


(comment
  (re-frame.subs/clear-subscription-cache!))

(defn todo-app
  []
  (ui/wrap-on
    :key-press
    (fn [handler s]
      (let [effects (handler s)]
        (println effects)
        (if (seq effects)
          ; handled by children?
          effects
          ; else handle it
          (do
            (println "main: keypress event: " s)
            (println "type: " (type s))
            (if (= s :escape)
              (do
                (println "caught escape!")
                (swap! memframe/text-boxes assoc :membrane.re-frame/focus nil)))
            [[:keydown s]]))))
    (ui/translate
     10 10
     (vertical-layout
      (ui/label (str "Hello from key8: " @(subscribe [:text])))
      (task-entry)
      (ui/spacer 0 20)
      (when (seq @(subscribe [:todos]))
        (task-list))
      (ui/spacer 0 20)
      (footer-controls)
      (stories)))))


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

(comment


  (def lorem-ipsum (clojure.string/join
                     "\n"
                     (repeatedly 800
                                 (fn []
                                   (clojure.string/join
                                      (repeatedly (rand-int 50)
                                                  #(rand-nth "abcdefghijklmnopqrstuvwxyz ")))))))

  (skia/run #(memframe/re-frame-app (#'test-scrollview lorem-ipsum)))
  (skia/run #(memframe/re-frame-app (#'stories))))


(comment
  object[clojure.lang.Atom
         0x2f0a3cc8
         {:status :ready,
          :val {[:todo-input "new-todo"] {:text "dddddddddddddddjjjjjjjjkkjjj",
                                          :textarea-state {:cursor 20,
                                                           :mpos [133.556640625 10.80859375],
                                                           :down-pos nil,
                                                           :select-cursor nil},
                                          :extra {[[[(get [:todo-input "new-todo"]) :text]
                                                    [(get [:todo-input "new-todo"])
                                                     :textarea-state
                                                     [(keypath :cursor) (nil->val 0)]]
                                                    [(fn-call (= focus (clojure.core/into arg-path-text-16264 [])))]
                                                    [(get [:todo-input "new-todo"]) :font]
                                                    [(get [:todo-input "new-todo"]) :textarea-state (keypath :down-pos)]
                                                    [(get [:todo-input "new-todo"]) :textarea-state (keypath :mpos)]
                                                    [(constant true)]
                                                    [(get [:todo-input "new-todo"])
                                                     :textarea-state
                                                     (keypath :select-cursor)]]
                                                   :$last-click] [1598644212128 [133.556640625 10.80859375]]}},
                :membrane.re-frame/focus [(get [:todo-input "new-todo"]) :text],
                [:todo-input 1] {:text "jkkjddjkkkfffff∂∂∂∂jkkkkkkk",
                                 :textarea-state {:cursor 27,
                                                  :mpos [145.359375 14.484375],
                                                  :down-pos nil,
                                                  :select-cursor nil},
                                 :extra {[[[(get [:todo-input 1]) :text]
                                           [(get [:todo-input 1]) :textarea-state [(keypath :cursor) (nil->val 0)]]
                                           [(fn-call (= focus (clojure.core/into arg-path-text-16264 [])))]
                                           [(get [:todo-input 1]) :font]
                                           [(get [:todo-input 1]) :textarea-state (keypath :down-pos)]
                                           [(get [:todo-input 1]) :textarea-state (keypath :mpos)]
                                           [(constant true)]
                                           [(get [:todo-input 1]) :textarea-state (keypath :select-cursor)]]
                                          :$last-click] [1598644168468 [145.359375 14.484375]]}}}}])