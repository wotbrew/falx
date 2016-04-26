(ns falx.impl.time
  (:require [falx.schedule :as sched]
            [falx.time :as time]
            [falx.action :as action]))

(def default-progress-ms
  100)

(defn run-activities
  [g ms]
  (reduce-kv
    (fn [g id activity]
      (let [a (update activity :running-ms (fnil + 0) ms)
            progress (int (/ (:running-ms a 0) (:progress-every-ms a default-progress-ms)))]
        (cond-> (assoc-in g [:activities id] (assoc a :progress progress))
                (< (:progress a 0) progress)
                (action/action g {:type :progress-activity
                                  :activity activity})))
      g)
    g
    (:activities g)))

(defn play
  [g ms]
  (let [{:keys [time sim-schedule visual-schedule]} g
        time (time/play time ms)
        {:keys [sim-ms visual-ms]} time
        actions (concat (sched/get-until sim-schedule sim-ms)
                        (sched/get-until visual-schedule visual-ms))
        g (assoc g
            :time time
            :sim-schedule (sched/remove-until sim-schedule sim-ms)
            :visual-schedule (sched/remove-until visual-schedule visual-ms))]
    (reduce action/action g actions)))

(defmethod action/action :pass-time
  [g {:keys [ms]}]
  (play g ms))