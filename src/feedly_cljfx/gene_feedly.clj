(ns feedly-cljfx.gene-feedly
  (:require
    [clojure.core.cache :as cache]
    [cljfx.api :as fx]
    [cljfx.ext.list-view :as fx.ext.list-view]
    [feedly-cljfx.feedly :as feedly]
    [feedly-cljfx.views :as views]
    [feedly-cljfx.events :as events]))

(def *state
  (atom
    (fx/create-context
      {:text "{:a 1}"
       :stories
             ;(feedly/read-file)
             (feedly/read-file)
       :filtered-stories '()
       :title-selected 0}
      cache/lru-cache-factory)))



(comment
  (renderer))

(def event-handler
  (-> events/event-handler
      (fx/wrap-co-effects
        {:fx/context (fx/make-deref-co-effect *state)})
      (fx/wrap-effects
        {:context (fx/make-reset-effect *state)
         :dispatch fx/dispatch-effect})
         ;:http http-effect})
      (fx/wrap-async)))

(def renderer
  (fx/create-renderer
    :middleware (comp
                  fx/wrap-context-desc
                  (fx/wrap-map-desc (fn [text]
                                      {:fx/type views/root-view
                                       :text text})))
    :opts {:fx.opt/map-event-handler event-handler
           :fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                        (fx/fn->lifecycle-with-context %))}))



(fx/mount-renderer *state renderer)