(ns feedly-cljfx.views
  (:require
    [cljfx.api :as fx]
    [cljfx.ext.list-view :as fx.ext.list-view]
    [cljfx.lifecycle :as lifecycle]
    [cljfx.mutator :as mutator]
    [cljfx.prop :as prop]
    [feedly.search :as search]
    [feedly-membrane.htmlcleaner :as htmlcleaner]
    [feedly-cljfx.feedly :as feedly]
    [feedly-cljfx.events :as events]
    [feedly-cljfx.my-subs :as subs])
  (:import [javafx.scene.web WebView]
           [javafx.scene.input KeyCode KeyEvent]
           [javafx.application Platform]
           [java.util.concurrent Executors ThreadFactory]))


(defn left-pane [{:keys [fx/context]}]
  ;(println "left-pane: " context)
  (let [stories   (fx/sub-val context :stories)
        filtered  (fx/sub-ctx context subs/filtered-list)
        selected  (fx/sub-val context :title-selected)
        show-list (search/active-stories filtered stories)]
    {:fx/type fx.ext.list-view/with-selection-props
     :props {:selection-mode :single
             :on-selected-index-changed {:event/type ::events/select-title-num}
             :selected-index selected}
     ;:on-selected-item-changed {:event/type ::list-click}
     :desc {
            :fx/type :list-view
            :cell-factory {:fx/cell-type :list-cell
                           :describe (fn [path]
                                       {:text path})}
            :items
            (->> show-list
                (map :title))}}))
;(remove nil?)
;(filter #(clojure.string/includes? % "Unicorn")))}})

(defn text [s]
  " remember that htmlcleaner/parse-paste returns map: {:title, :content} "
  (:content (htmlcleaner/parse-page s)))


(def web-view-with-ext-props
  (fx/make-ext-with-props
    {:load-content (prop/make (mutator/setter
                                #(.loadContent (.getEngine ^WebView %1) %2))
                              lifecycle/scalar)}))

(defn get-engine [evt]
  (-> ^javafx.event.ActionEvent evt
      ^javafx.scene.Node (.getTarget)
      (.getScene)
      ^javafx.scene.web.WebView (.lookup "#web-viewer")
      .getEngine))

(defn execute-script! [h cmd]
  (.executeScript h cmd))


(def webviewer nil)

(defn right-pane2 [{:keys [fx/context]}]
  (let [title-num (fx/sub-val context :title-selected)
        show-list (fx/sub-ctx context subs/active-stories)
        story (-> (nth show-list title-num)
                  :content
                  :content)]
    (spit "/tmp/x.html" story)
    (println "right-pane2: entering...")
    {:fx/type :v-box
     :children [{:fx/type web-view-with-ext-props
                 :desc {:fx/type :web-view
                        :id "web-viewer"
                        :pref-height 1000
                        :pref-width 1500}
                 :props {:load-content story}}
                {:fx/type :button
                 :text "Scroll to top"
                 :on-action (fn [x]
                              (let [handle (get-engine x)]
                                (def webviewer handle)
                                (execute-script! handle "window.scrollTo(0,0)")))}
                {:fx/type :button
                 :text "Scroll down"
                 :on-action (fn [x]
                              (let [handle (get-engine x)
                                    ;result (.getDocument handle)
                                    result (execute-script! handle "true")]
                                (def webviewer handle)
                                ;(println (bean result))
                                (def webviewer-result result)
                                (println "scroll down: document; " result)))}]}))
                                ;(println (execute-script! handle "getDocument()"))))}]}))

(defn labeled-input [{:keys [label input]}]
  {:fx/type :v-box
   :spacing 5
   :children [{:fx/type :label
               :text label}
              input]})

(defn name-input [{:keys [value]}]
  {:fx/type labeled-input
   :label "Search"
   :input {:fx/type :text-field
           :text-formatter {:fx/type :text-formatter
                            :value-converter :default
                            :value value
                            :on-value-changed {:event/type ::events/search-text-changed}}}})

(defn stats-header [{:keys [fx/context]}]
  (let [stories   (fx/sub-val context :stories)
        title-num (fx/sub-val context :title-selected)
        filtered  (fx/sub-ctx context subs/filtered-list)]
    {:fx/type :v-box
     :children [{:fx/type :label
                 :text (format "# of stories: %d (selected=%d); filtered: %d"
                               (count stories)
                               title-num
                               (count filtered))}]}))

(defn root-view [{:keys [text]}]
  {:fx/type :stage
   :width 960
   :height 400
   :showing true
   :scene {:fx/type :scene
           :accelerators {[:j] {:event/type ::events/next-story}
                          [:k] {:event/type ::events/prev-story}}
           :root {:fx/type :v-box
                  :padding 40
                  :spacing 20
                  :children [{:fx/type name-input
                              :value ""}
                             {:fx/type stats-header}
                             {:fx/type :grid-pane
                              :padding 10
                              :hgap 10
                              :column-constraints [{:fx/type :column-constraints
                                                    :percent-width 100/4}
                                                   {:fx/type :column-constraints
                                                    :percent-width (* 3 100/4)}]
                              :row-constraints [{:fx/type :row-constraints
                                                 :percent-height 100}]
                              :children [{:fx/type left-pane}
                                         {:fx/type right-pane2
                                          :grid-pane/column 1}]}]}}})