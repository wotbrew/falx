(ns falx.ui.main
  (:require [falx.ui.widget :as widget]
            [falx.ui :as ui]))

(defn left-panel
  [ui frame sh]
  (let [x 0
        y 0
        w (* 32 4)]
    (widget/panel
      [(widget/block [(+ x w) y 32 sh])])))

(defn right-panel
  [ui frame sw sh]
  (let [y 0
        w (* 32 4)
        x (- sw w)]
    (widget/panel
      [(widget/block [(- x 32) y 32 sh])])))

(defn bottom-panel
  [ui frame sw sh]
  (let [x (* 32 4)
        w (- sw x x)
        y (- sh (* 32 5))
        h 32]
    (widget/panel
      [(widget/block [x y w h])])))

(defmethod ui/screen :screen/main
  [ui frame]
  (let [display (:display frame)
        [w h :as size] (:size display)]
    (widget/as :screen/main
      (widget/screen [(left-panel ui frame h)
                      (right-panel ui frame w h)
                      (bottom-panel ui frame w h)
                      (widget/text (:input frame) [0 32 256 0])
                      (widget/fps frame)]))))

(defmethod widget/update-ui-screen :screen/main
  [ui widget frame]
  (let [keyboard (-> frame :input :keyboard)]
    (if (contains? (:hit keyboard) :e)
      (ui/select-screen ui :screen/edit)
      ui)))

;;tree to list
;; parent -> children
;; id state rel
;; get widget for id
;; state change -> refresh widget