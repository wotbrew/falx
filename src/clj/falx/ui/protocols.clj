(ns falx.ui.protocols)

(defprotocol IScreenObject
  (-handle! [this frame x y w h]))

(defprotocol IMeasure
  (measure [this frame x y w h]))
