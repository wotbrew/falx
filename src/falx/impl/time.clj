(ns falx.impl.time
  (:require [falx.schedule :as sched]
            [falx.time :as time]
            [falx.action :as action]))

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