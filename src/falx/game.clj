(ns falx.game
  (:require [falx.world :as world]
            [falx.frame :as frame]
            [clojure.core.async :as async :refer [go go-loop >! <! chan]]
            [clojure.tools.logging :refer [debug]]
            [falx.ui :as ui])
  (:refer-clojure :exclude [empty])
  (:import (java.util UUID)))

(defn bus-chan
  []
  (chan 512))

(defn bus-pub
  [bus-mult]
  (let [c (chan)]
    (async/tap bus-mult c)
    (async/pub c :type)))

(defn world-agent
  [bus-chan]
  (let [c (async/chan 1024 cat)]
    (async/pipe c bus-chan false)
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

(defn publish-coll!
  [game msgs]
  (async/onto-chan (:bus-chan game) msgs false))

(defn publish!
  ([game msg]
   (async/go
     (>! (:bus-chan game) msg)))
  ([game msg & msgs]
   (publish-coll! game (cons msg msgs))))

(defn close!
  [game]
  (debug "Closing game" (:id game))
  (let [{:keys [bus-chan bus-pub]} game]
    (publish! game {:type :game.event/closing
                    :id (:id game)})
    (async/close! bus-chan)
    (async/unsub-all bus-pub)
    (debug "Game closed" (:id game))
    nil))

(defn game
  []
  (let [id (str (UUID/randomUUID))
        bus-chan (bus-chan)
        bus-mult (async/mult bus-chan)
        bus-pub (bus-pub bus-mult)
        world-agent (world-agent bus-chan)
        ui-atom (atom {})
        ui-state-agent (agent {})]
    (debug "Created game" id)
    (doto
      {:id             id
       :bus-chan       bus-chan
       :bus-mult       bus-mult
       :bus-pub        bus-pub
       :world-agent    world-agent
       :ui-atom        ui-atom
       :ui-state-agent ui-state-agent}
      (publish! {:type :game.event/starting
                 :id id}))))

(defn get-ui
  [game]
  @(:ui-atom game))

(defn get-ui-state
  [game]
  @(:ui-state-agent game))

(defn update-ui-state!
  ([game f]
   (send (:ui-state-agent game) f)
   nil)
  ([game f & args]
   (update-ui-state! game #(apply f % args))))

(defn get-world
  [game]
  @(:world-agent game))

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

(defn get-at
  [game cell]
  (world/get-at (get-world game) cell))

(defn solid-at?
  [game cell]
  (world/solid-at? (get-world game) cell))

(defn replace-actor!
  [game actor]
  (update-world! game world/replace-actor actor))

(defn add-actor!
  [game actor]
  (replace-actor! game actor))

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
   (let [{:keys [ui-atom ui-state-agent]} game
         ui @ui-atom
         ui-state @ui-state-agent]
     (publish-coll! game (ui/get-events ui frame))
     (let [nui (swap! ui-atom ui/process frame ui-state)]
       (update-ui-state! game ui/next-state nui))
     nil)))

(defn plug!
  ([game chan]
   (async/pipe chan (:bus-chan game) false))
  ([game chan & chs]
   (doseq [c (cons chan chs)]
     (plug! game c))))

(defn sub!
  ([game chan]
   (let [bus (:bus-mult game)]
     (async/tap bus chan)
     chan))
  ([game type chan]
   (let [bus (:bus-pub game)]
     (async/sub bus type chan)
     chan)))

(defn sub
  ([game]
   (let [c (async/chan)]
     (sub! game c)))
  ([game type]
   (let [c (async/chan)]
     (sub! game type c)))
  ([game type & types]
   (async/merge (map #(sub game %) (cons type types)))))

(defn subxf
  ([game xform]
   (sub! game (chan 32 xform)))
  ([game xform type]
   (sub! game type (chan 32 xform)))
  ([game xform type & types]
   (async/merge (map #(subxf game xform %) (cons type types)))))

(defn subfn!
  ([game f]
   (let [c (sub game)]
     (go-loop
       []
       (when-some [x (<! c)]
         (f x)
         (recur)))))
  ([game type f]
   (let [c (sub game type)]
     (go-loop
       []
       (when-some [x (<! c)]
         (f x)
         (recur))))))