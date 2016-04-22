(ns falx.time
  "Functions on a map that represents time itself in the game.
  Time has two modes
  - :realtime
  - :turns

  Time can be passed with the fn `play` which increases the sim time and the visual time.
  The sim time only moves forward in :realtime mode or at the end of each turn via `next-round`."
  (:refer-clojure :exclude [time]))

(defn time
  "Returns an intital time map"
  ([]
   (time (System/currentTimeMillis)))
  ([visual-ms]
   {:mode :realtime
    :round-done []
    :round :controlled
    :visual-ms visual-ms
    :visual-delta-ms 0
    :sim-ms 0}))

(defn realtime?
  "Is the time mode :realtime?"
  [time]
  (= (:mode time) :realtime))

(defn turns?
  "Is the time mode :turns?"
  [time]
  (= (:mode time) :turns))

(defn control?
  "Is the round player controllable"
  [time]
  (= (:round time) (:controlled time)))

(defn play
  "Passes `ms` worth of time. If the game is :turns mode,
  the sim time will not progress."
  [time ms]
  (if (turns? time)
    (update :visual-ms + ms)
    (let [{:keys [visual-ms sim-ms]
           :or {visual-ms 0
                sim-ms 0}} time]
      (assoc time :sim-ms (+ sim-ms ms)
                  :visual-ms (+ visual-ms ms)
                  :delta-ms ms))))

(def sim-speed
  "The number of sim-ms to add to the time when a turn is completed."
  1000)

(defn next-round
  "Moves the round to from :controlled to :uncontrolled. If the turn is completed,
  progresses the sim time."
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
                    :sim-ms (+ (:sim-ms time 0) sim-speed))))))

(defn enter-turns
  "Changes the time into :turns mode."
  [time round]
  (assoc time
    :mode :turns
    :round round
    :round-done []))

(defn enter-realtime
  "Changes the time into :realtime mode."
  [time]
  (assoc time
    :mode :realtime
    :round :controlled
    :round-done []))