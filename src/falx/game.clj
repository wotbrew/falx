(ns falx.game
  (:require [falx.world :as world]
            [falx.frame :as frame]
            [clojure.core.async :as async :refer [go go-loop >! <! chan]]
            [clojure.tools.logging :refer [debug]]
            [falx.ui :as ui])
  (:refer-clojure :exclude [empty])
  (:import (java.util UUID)))

(defn event-chan
  []
  (chan 512))

(defn event-pub
  [event-mult]
  (let [c (chan)]
    (async/tap event-mult c)
    (async/pub c :type)))

(defn world-agent
  [event-chan]
  (let [c (async/chan 1024 cat)]
    (async/pipe c event-chan false)
    (doto
      (agent (world/world []))
      (add-watch ::publish-events
                 (fn [_ a old new]
                   (when (and (not (identical? old new))
                              (seq (:events new)))
                     (send a
                           #(let [{:keys [world events]} (world/split-events %)]
                             ;;gotta block to preserve order, can I use async/put! here?
                             ;;it doesn't need to be syncnronous as long as order of dispatch is preserved
                             (async/>!! c events)
                             world))))))))

(defn game
  []
  (let [id (str (UUID/randomUUID))
        event-chan (event-chan)
        event-mult (async/mult event-chan)
        event-pub (event-pub event-mult)
        world-agent (world-agent event-chan)
        ui-agent (agent {})
        ui-state-agent (agent {})]
    (debug "Created game" id)
    {:id             id
     :event-chan     event-chan
     :event-mult     event-mult
     :event-pub      event-pub
     :world-agent    world-agent
     :ui-agent       ui-agent
     :ui-state-agent ui-state-agent}))

(defn close!
  [game]
  (debug "Closing game" (:id game))
  (let [{:keys [event-chan event-pub]} game]
    (async/close! event-chan)
    (async/unsub-all event-pub)
    (debug "Game closed" (:id game))
    nil))

(defn publish-coll!
  [game event-coll]
  (async/onto-chan (:event-chan game) event-coll false))

(defn publish!
  ([game event]
   (async/go
     (>! (:event-chan game) event)))
  ([game event & events]
   (publish-coll! game (cons event events))))

(defn get-world
  [game]
  @(:world-agent game))

(defn get-ui
  [game]
  @(:ui-agent game))

(defn get-ui-state
  [game]
  @(:ui-state-agent game))

(defn update-world!
  ([game f]
   (send (:world-agent game) f)
   nil)
  ([game f & args]
   (update-world! game #(apply f % args))))

(defn get-actor
  [game id]
  (world/get-actor (get-world game) id))

(defn query-actors
  ([game k v]
   (world/query-actors (get-world game) k v))
  ([game k v & kvs]
   (apply world/query-actors (get-world game) k v kvs)))

(defn replace-actor!
  [game actor]
  (update-world! game world/replace-actor actor))

(defn add-actor!
  [game actor]
  (replace-actor! game actor))

(defn merge-actor!
  [game actor]
  (update-world! game world/merge-actor actor))

(defn remove-actor!
  [game id]
  (update-world! game world/remove-actor id))

(defn update-actor!
  ([game id f]
   (update-world! game world/update-actor id f))
  ([game id f & args]
   (update-actor! game id #(apply f % args))))

(defn get-current-frame
  [game]
  (frame/get-current-frame (get-world game)))

(defn process-frame!
  ([game]
   (process-frame! game (get-current-frame game)))
  ([game frame]
   (publish! game {:type :game.event/frame
                   :frame frame
                   :silent? true})
   (let [{:keys [ui-agent ui-state-agent]} game
         ui @ui-agent
         ui-state @ui-state-agent]
     (publish-coll! game (ui/get-events ui frame))
     (send ui-agent ui/process frame ui-state)
     (send ui-state-agent ui/next-state ui)
     nil)))

(defn install-event-xform
  ([game type xform]
   (install-event-xform game type xform 1))
  ([game type xform n]
   (let [event-pub (:event-pub game)
         event-chan (:event-chan game)
         c (async/chan)]
     (async/sub event-pub type c)
     (async/pipeline n event-chan xform c)
     game)))

(defn install-event-fn
  ([game type f]
   (install-event-fn game type f 1))
  ([game type f n]
   (install-event-xform game type (keep f) n)))

(defn install-event-xform-blocking
  ([game type xform]
   (install-event-xform-blocking game type xform 1))
  ([game type xform n]
   (let [event-pub (:event-pub game)
         event-chan (:event-chan game)
         c (async/chan)]
     (async/sub event-pub type c)
     (async/pipeline-blocking n event-chan xform c)
     game)))

(defn install-event-fn-blocking
  ([game type f]
   (install-event-fn-blocking game type f 1))
  ([game type f n]
   (install-event-xform-blocking game type (keep f) n)))
