(ns feedly-cljfx.events
  (:require
    [cljfx.api :as fx]
    [feedly.search]
    [feedly-cljfx.my-subs :as subs])
  (:import [java.util UUID]
           [javafx.scene.input KeyCode KeyEvent]))

(defn parse-int [s]
  (Integer/parseInt (re-find #"\A-?\d+" s)))


(defmulti event-handler :event/type)

(defmethod event-handler ::text-changed [{:keys [fx/event]}]
  [[:text event]])

(defmethod event-handler ::search-text-changed [{:keys [fx/context fx/event]}]
  (println "::search-text-changed: " event)
  {:context (fx/swap-context context assoc :search-text event)})

(defmethod event-handler ::select-title-num [{:keys [fx/context fx/event]}]
  (println "::select-title-num: " (str event)  event (type event))
  {:context (fx/swap-context context assoc :title-selected event)})

(defmethod event-handler ::next-story [{:keys [fx/context fx/event]}]
  (let [title-selected (fx/sub-val context :title-selected)
        stories (fx/sub-ctx context subs/active-stories)]
    (println (format "::next-story: %d of %d" title-selected (count stories)
                     (str event)  event (type event)))
    {:context (fx/swap-context context assoc :title-selected
                               (min (inc title-selected)
                                    (inc (count stories))))}))

(defmethod event-handler ::prev-story [{:keys [fx/context fx/event]}]
  (let [title-selected (fx/sub-val context :title-selected)]
    (println "::prev-story: " title-selected (str event)  event (type event))
    {:context (fx/swap-context context assoc :title-selected
                               (max (dec title-selected)
                                    0))}))



(defmethod event-handler :default [event]
  ; don't print the huge stories map
  (println "::default-event-handler")
  (prn (update-in event [:fx/context :cljfx.context/m] dissoc :stories)))

