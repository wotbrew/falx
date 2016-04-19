(ns falx.process
  (:require [clojure.core.async :as async :refer [chan go <! >! go-loop]]
            [clojure.tools.logging :refer [error]]
            [falx.action :as action]))

(defn process
  [g]
  (let [in (chan 32)
        out (chan 32)
        state (volatile! g)]
    (go-loop
      [g g]
      (vreset! state g)
      ;;get outbound stuff and put it on out
      (when-some [[[type payload] c] (<! in)]
        (case type
          :stop (do (>! c true) (async/close! in) (async/close! out))
          :action (let [g2 (try
                             (reduce action/action g payload)
                             (catch Throwable e
                               (error e "an error occurred executing actions:" payload)
                               g))]
                    (>! c g2)
                    (recur g2)))))
    {:in in
     :out out
     :state state}))

(defn get-state
  [pr]
  @(:state pr))

(defn- request!
  [pr req]
  (let [c (chan 1)
        in (:in pr)]
    (go
      (when (nil? (>! in [req c]))
        (async/close! c)))
    c))

(defn stop!
  [pr]
  (request! pr [:stop]))

(defn actions!
  [pr action-coll]
  (request! pr [:action action-coll]))

(defn action!
  ([pr action]
   (actions! pr [action]))
  ([pr action & more]
   (actions! pr (cons action more))))