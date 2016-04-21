(ns falx.time
  (:refer-clojure :exclude [time]))

(defn time
  ([]
   (time (System/currentTimeMillis)))
  ([visual-ms]
   {:mode :realtime
    :round-done []
    :round :controlled
    :visual-ms visual-ms
    :sim-ms 0}))

(defn realtime?
  [time]
  (= (:mode time) :realtime))

(defn turns?
  [time]
  (= (:mode time) :turns))

(defn control?
  [time]
  (= (:round time) (:controlled time)))

(defn play
  [time ms]
  (if (turns? time)
    (update :visual-ms + ms)
    (let [{:keys [visual-ms sim-ms]} time]
      (assoc time :sim-ms (+ sim-ms ms)
                  :visual-ms (+ visual-ms ms)))))

(def sim-speed
  1000)

(defn next-round
  [time]
  (if (realtime? time)
    time
    (let [{:keys [round-done round]} time
          next-round (case round
                       :controlled :uncontrolled
                       :uncontrolled :controlled)]
      (if (empty? round-done)
        (assoc time :round next-round
                    :round-done [round])
        (assoc time :round next-round
                    :round-done []
                    :sim-ms (+ (:sim-ms time) sim-speed))))))

(defn enter-turns
  [time round]
  (assoc time
    :mode :turns
    :round round
    :round-done []))

(defn enter-realtime
  [time]
  (assoc time
    :mode :realtime
    :round :controlled
    :round-done []))