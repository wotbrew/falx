(ns falx.event
  (:require [clojure.core.async :as async]))

(def event-chan (async/chan 64))

(def event-pub (async/pub event-chan :type (fn [_] (async/buffer 64))))

(defn debug-event!
  [event]
  (println "Event:" (:type event)))

(defn publish-event!
  [event]
  (debug-event! event)
  (async/go (async/>! event-chan event)))

(defn publish-events!
  [events]
  (run! debug-event! events)
  (async/onto-chan event-chan events false))

(def listeners
  (agent {}))

(defn subscribe!
  [topic key f]
  (send
    listeners
    (fn ! [m]
      (let [ex (get m [topic key])]
        (if ex
          (do
            (async/unsub event-pub topic ex)
            (async/close! ex)
            (send listeners !)
            (dissoc m [topic key]))
          (assoc m [topic key]
                   (let [c (async/chan)]
                     (async/sub event-pub topic c)
                     (async/go-loop
                       []
                       (when-some [event (async/<! c)]
                         (try
                           (f event)
                           (catch Throwable e
                             (println "Error in subscriber:" topic key e)))
                         (recur)))
                     c)))))))