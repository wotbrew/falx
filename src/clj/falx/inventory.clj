(ns falx.inventory
  (:require [falx.ui :as ui]
            [falx.gdx :as gdx]))

(def icon
  (gdx/texture-region ui/gui 128 0 32 32))

(ui/defscene :inventory
  ui/back-handler
  ui/breadcrumbs
  (ui/center
    (ui/resize
      320 280
      (ui/stack
        (ui/fancy-box 2)
        (ui/center "inventory")))))

(defmethod ui/scene-name :continue [_] "Inventory")