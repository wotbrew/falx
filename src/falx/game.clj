(ns falx.game
  (:require [falx.world :as world]
            [clj-gdx :as gdx]
            [clojure.core.async :as async :refer [go >! <! chan]]
            [clojure.tools.logging :refer [debug]])
  (:refer-clojure :exclude [empty]))

(defonce event-chan (chan 512))

(defonce event-mult (async/mult event-chan))

(defonce event-pub
  (let [c (chan)]
    (async/tap event-mult c)
    (async/pub c :type)))

(defn publish!
  [event]
  (go (>! event-chan event)))

(defn publish-coll!
  [event-coll]
  (async/onto-chan event-chan event-coll false))

(defonce pending-world-events-chan
  (let [c (async/chan 1024 cat)]
    (async/pipe c event-chan false)
    c))

(def world
  (doto
    (agent (world/world []))
    (add-watch ::publish-events
               (fn [_ _ old new]
                 (when (and (not (identical? old new))
                            (seq (:events new)))
                   (let [{:keys [world events]} (world/split-events new)]
                     ;;gotta block to preserve order, can I use async/put! here?
                     ;;it doesn't need to be syncnronous as long as order of dispatch is preserved
                     (doseq [event events]
                       (debug "Event:" (:type event)))
                     (async/>!! pending-world-events-chan events)
                     world))))))

(defn update-world!
  ([f]
   (send world f)
   nil)
  ([f & args]
   (update-world! #(apply f % args))))

(defn get-actor
  [id]
  (world/get-actor @world id))

(defn query-actors
  ([k v]
   (world/query-actors @world k v))
  ([k v & kvs]
   (apply world/query-actors @world k v kvs)))

(defn replace-actor!
  [actor]
  (update-world! world/replace-actor actor))

(defn add-actor!
  [actor]
  (replace-actor! actor))

(defn merge-actor!
  [actor]
  (update-world! world/merge-actor actor))

(defn remove-actor!
  [id]
  (update-world! world/remove-actor id))

(defn update-actor!
  ([id f]
   (update-world! world/update-actor id f))
  ([id f & args]
   (update-actor! id #(apply f % args))))

(def input
  (agent {:mouse gdx/default-mouse
          :keyboard gdx/default-keyboard}))

(def screen
  (agent nil))