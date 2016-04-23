(ns falx.ui.mouse)

(defn- translate-point
  [[x y] [cx cy] [cw ch]]
  [(int (/ (+ cx x) cw))
   (int (/ (+ cy y) ch))])

(defn get-state
  [display viewport point]
  (let [camera (:camera viewport)
        wpoint (translate-point point
                                (:point camera [0 0])
                                (:cell-size display [32 32]))]
    {:point point
     :cell {:point wpoint
            :level (:level viewport)}}))