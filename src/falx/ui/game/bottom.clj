(ns falx.ui.game.bottom
  (:require [falx.game :as g]
            [falx.ui :as ui]))

(defn get-rect
  [sw sh]
  [0 (- sh (* 4 32)) sw (* 4 32)])

(defn get-panel
  [x y w h]
  {:id                                    ::panel
   :type                                  :element/panel
   :rect                                  [x y w h]
   :ui-root?                              true
   :ui-children                           (->> [(ui/pixel [0 0 w h] {:color ui/black})
                                                (ui/box [0 0 w h] {:color ui/gray})]
                                               (ui/relative-to x y))
   ;;handles
   [:handles? :event/screen-size-changed] true})

(defmethod g/uhandle [::panel :event/screen-size-changed]
  [g a {:keys [size]}]
  (assoc g :rect (get-rect (first size) (second size))))

(defn get-actors
  [g sw sh]
  (let [[x y w h] (get-rect sw sh)]
    [(get-panel x y w h)]))