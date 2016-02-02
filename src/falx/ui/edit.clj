(ns falx.ui.edit
  (:require [falx.ui.widget :as widget]
            [falx.ui :as ui]))

(defmethod ui/screen :screen/edit
  [ui frame]
  {:id :screen/edit
   :type     :ui.type/screen
   :elements [#_(widget/text (:fps frame) [0 0 90 0])
              (widget/box [32 32 32 32])
              (widget/panel [64 32 0 0] [(widget/box [0 0 96 32])])]})