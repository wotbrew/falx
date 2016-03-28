(ns falx.frame)

(defrecord FrameTime [^double delta ^long fps])

(defn frame-time
  [delta fps]
  (->FrameTime delta fps))

(defrecord Frame [game display frame-time])

(defn frame
  [game display frame-time]
  (->Frame game display frame-time))

(defn get-delta
  [frame]
  (-> frame :frame-time :delta))